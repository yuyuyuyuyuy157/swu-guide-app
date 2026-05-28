package com.nav.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import cn.hutool.crypto.SecureUtil;
import com.nav.constant.MessageConstant;
import com.nav.context.BaseContext;
import com.nav.dto.*;
import com.nav.entity.InvitationCode;
import com.nav.entity.User;
import com.nav.entity.UserAudioSetting;
import com.nav.exception.BaseException;
import com.nav.exception.InvalidInvitationCodeException;
import com.nav.exception.PhoneAlreadyExistsException;
import com.nav.mapper.InvitationCodeMapper;
import com.nav.mapper.UserAudioSettingMapper;
import com.nav.mapper.UserMapper;
import com.nav.properties.JwtProperties;
import com.nav.service.UserService;
import com.nav.utils.JwtUtil;
import com.nav.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@Service
// 🎯 继承 ServiceImpl，全面激活 Users 主表原生的 CRUD 武器库
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InvitationCodeMapper invitationCodeMapper;

    @Autowired
    private UserAudioSettingMapper userAudioSettingMapper;

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 用户端 - 密码登录
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        String password = userLoginDTO.getPassword();

        // 1. 🎯 修正：剔除已被剥离的 is_deleted 条件，纯净根据手机号精确定位
        User user = this.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));

        // 2. 账号存在性校验
        if (user == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 3. 验证动态加盐 HASH
        String saltPassword = password + phone;
        String inputHash = SecureUtil.md5(saltPassword);
        if (!inputHash.equals(user.getPasswordHash())) {
            throw new BaseException(MessageConstant.PASSWORD_ERROR);
        }

        // 4. 账号状态校验
        if (user.getStatus() == 2) { // 2-冻结
            throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 5. 数据脱敏与 Token 颁发
        String displayPhone = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());

        String realToken = JwtUtil.createToken(claims);

        return UserLoginVO.builder()
                .token(realToken)
                .userId(user.getId().toString())
                .phone(displayPhone)
                .avatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : "https://api.swu-guide-app.com/static/default-avatar.png")
                .role(user.getRole().toLowerCase())
                .playSettings(new HashMap<>())
                .build();
    }

    /**
     * 用户端 - 邀请码注册
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterDTO userRegisterDTO) {
        String phone = userRegisterDTO.getPhone();
        String password = userRegisterDTO.getPassword();
        String code = userRegisterDTO.getInvitationCode();

        // 1. 唯一性校验
        Long count = this.baseMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
        if (count > 0) {
            throw new PhoneAlreadyExistsException(MessageConstant.PHONE_EXISTS);
        }

        // 2. 🎯 修正：剔除不存在的 is_deleted 字段，建立纯净的新用户实体
        User newUser = User.builder()
                .phone(phone)
                .role("USER")
                .status(1) // 1-正常
                .passwordHash(SecureUtil.md5(password + phone))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        this.save(newUser); // 利用 MP 插入数据，成功后 newUser 的 id 属性会被底层 JDBC 自动回填

        // 3. 🎯 降维重构：平替已被清空的 useCode 复杂 SQL 语句
        // 纯 Java 条件链式控制：只有当邀请码匹配、且未被使用（status = 1）时，才原子性推进核销
        boolean hasCodeVerified = invitationCodeMapper.update(null,
                new LambdaUpdateWrapper<InvitationCode>()
                        .set(InvitationCode::getStatus, 2) // 变更状态为 2 (已使用)
                        .set(InvitationCode::getUsedByUserId, newUser.getId())
                        .set(InvitationCode::getUsedAt, LocalDateTime.now())
                        .eq(InvitationCode::getCode, code)
                        .eq(InvitationCode::getStatus, 1)   // 乐观锁：确保必须是未使用状态才可核销
        ) > 0;

        if (!hasCodeVerified) {
            throw new InvalidInvitationCodeException(MessageConstant.INVALID_CODE);
        }
    }

    /**
     * 用户端 - 用户信息展示
     */
    @Override
    public UserVO getCurrentInfo(Long userId) {
        // 1. 根据主键查询数据库实体
        User user = userMapper.selectById(userId); // 假设你用的是 MyBatis-Plus 的自带方法，或者你原生的 getById
        if (user == null) {
            throw new BaseException("未能查询到当前用户信息");
        }

        // 2. 🎯 核心逻辑一：手机号高敏感数据脱敏（13812345678 -> 138****5678）
        String rawPhone = user.getPhone();
        String maskedPhone = "未知手机号";
        if (rawPhone != null && rawPhone.length() == 11) {
            // 利用正则表达式，保留前3后4，中间替换为星号
            maskedPhone = rawPhone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        }

        // 3. 🎯 核心逻辑二：将 LocalDateTime 转换为前端易读的字符串格式
        String formattedCreateTime = "";
        if (user.getCreatedAt() != null) {
            formattedCreateTime = user.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }

        // 4. 🎯 核心逻辑三：安全拼装 VO 下发
        return UserVO.builder()
                // 必须转为 String，防止 Long 型雪花算法 ID 传给前端导致 JS 精度丢失
                .userId(String.valueOf(user.getId()))
                .phone(maskedPhone)
                // 做好判空防御，防止新注册用户没有头像导致前端报 null
                .avatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : "")
                // 枚举转换为字符串（如果你的 role 在实体类中是枚举的话）
                .role(user.getRole() != null ? user.getRole().toString() : "USER")
                .createTime(formattedCreateTime)
                .build();
    }

    /**
     * 用户端 - 修改当前密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UserEditPasswordDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();

        // 1. 获取完整元数据
        User user = this.getById(currentUserId);
        if (user == null) {
            throw new BaseException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 校验旧密码
        String oldInputHash = SecureUtil.md5(dto.getOldPassword() + user.getPhone());
        if (!oldInputHash.equals(user.getPasswordHash())) {
            throw new BaseException("原密码输入错误，请重新确认");
        }

        // 3. 哈希碰撞判定
        String newInputHash = SecureUtil.md5(dto.getNewPassword() + user.getPhone());
        if (newInputHash.equals(user.getPasswordHash())) {
            throw new BaseException("新密码不能与原密码相同");
        }

        // 4. 更新核心字段
        user.setPasswordHash(newInputHash);
        user.setPasswordLastChangedAt(LocalDateTime.now());

        this.updateById(user);
        log.info("用户 [ID: {}] 密码修改成功，安全流已刷新。", currentUserId);
    }

    /**
     * 管理端 - 管理员登录
     */
    @Override
    public AdminLoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();

        // 1. 联合查询角色定位（从统合后的 users 表抓取具有管理员权限的角色）
        User admin = this.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getPhone, username)
                .eq(User::getRole, "ADMIN"));

        if (admin == null) {
            throw new BaseException("管理员账号不存在");
        }

        // 2. 密码加盐哈希比对
        String inputHash = SecureUtil.md5(password + username);
        if (!inputHash.equals(admin.getPasswordHash())) {
            throw new BaseException("管理员密码错误");
        }

        // 3. 分配管理员级别的离线凭证令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", admin.getId());
        claims.put("role", "ADMIN");

        String token = JwtUtil.createToken(
                claims,
                jwtProperties.getAdminTtl(),
                jwtProperties.getAdminSecretKey()
        );

        return AdminLoginVO.builder()
                .token(token)
                .adminId(admin.getId().toString())
                .name("高级管理员(" + username.substring(0, 3) + ")")
                .build();
    }

    /**
     * 🎯 跨表核心修复：将偏好配置数据优雅导向 user_audio_settings 附属子表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateAudioSetting(Long userId, UserAudioSettingDTO dto) {
        log.info("🎯 开始动态更新用户偏好设置，用户ID: {}", userId);

        if (this.getById(userId) == null) {
            throw new BaseException("用户账户不存在");
        }

        // 组装副表实体数据模型
        UserAudioSetting setting = UserAudioSetting.builder()
                .userId(userId)
                .autoPlayEnabled(Boolean.TRUE.equals(dto.getAutoPlay()) ? 1 : 0)
                .repeatPolicy(dto.getRepeatMode())
                .switchPolicy(dto.getPlaySwitchMode())
                .backgroundPlayEnabled(Boolean.TRUE.equals(dto.getBackgroundPlay()) ? 1 : 0)
                .updatedAt(LocalDateTime.now())
                .build();

        // 🎯 降维秒杀：利用我们在前一步扩展的 insertOrUpdate 机制，有则覆盖更新，无则创建插入
        userAudioSettingMapper.insertOrUpdate(setting);
        log.info("✅ 用户ID: {} 的附属音频设置已成功持久化隔离落库。", userId);
    }

    /**
     * 🎯 跨表核心修复：从 user_audio_settings 专属副表回填偏好参数
     */
    @Override
    public UserAudioSettingVO getAudioSetting(Long userId) {
        log.info("🎯 开始调取用户音频副表偏好配置数据，用户ID: {}", userId);

        // 去副表定向查找单条数据
        UserAudioSetting setting = userAudioSettingMapper.selectById(userId);

        // 如果是新用户，副表可能没数据 (setting == null)，需要一套内存默认值兜底
        if (setting == null) {
            log.info("💡 用户ID: {} 暂无副表配置，触发内存柔性默认值兜底", userId);
            return UserAudioSettingVO.builder()
                    .autoPlay(true)               // 默认开启自动播放
                    .repeatMode(1)                // 默认每个景点只播放一次
                    .playSwitchMode(1)            // 默认播完再切（对应你原本想给的 1）
                    .backgroundPlay(true)         // 默认开启后台播放
                    .playSpeed(1.0f)              // 默认1倍速
                    .backwardForwardDuration(15)  // 默认快进快退15秒
                    .build();
        }

        // 副表有数据，将数据库的 Tinyint (0/1) 转换为 VO 的 Boolean，并对齐字段名
        return UserAudioSettingVO.builder()
                .autoPlay(setting.getAutoPlayEnabled() != null && setting.getAutoPlayEnabled() == 1)
                .repeatMode(setting.getRepeatPolicy())
                .playSwitchMode(setting.getSwitchPolicy()) // 🎯 修正：将数据库的 switchPolicy 映射到 VO 的 playSwitchMode
                .backgroundPlay(setting.getBackgroundPlayEnabled() != null && setting.getBackgroundPlayEnabled() == 1)
                // 如果你副表里还有下面这两个字段，记得也顺手带上；如果没有，可以给个固定默认值
                .playSpeed(1.0f)
                .backwardForwardDuration(15)
                .build();
    }
    @Value("${nav.upload.base-path}")
    private String basePath;

    @Value("${nav.upload.url-prefix}")
    private String urlPrefix;
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserAvatarVO uploadAvatar(Long userId, MultipartFile file) {
        log.info("🚀 开始执行本地文件上传与数据库回填，用户ID: {}", userId);

        try {
            // 1. 提取文件后缀名 (如 .jpg, .png)
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".png";

            // 2. 构造唯一文件名，防止重名覆盖
            String uniqueFileName = UUID.randomUUID().toString().replace("-", "") + extension;

            // 3. 检查并创建本地物理目录（如果目录不存在则创建）
            File dir = new File(basePath);
            if (!dir.exists()) {
                dir.mkdirs(); // 级联创建目录
            }

            // 4. 执行文件落盘操作
            File targetFile = new File(basePath + uniqueFileName);
            file.transferTo(targetFile);

            // 5. 拼装网络访问的 URL 路径
            String targetUrl = urlPrefix + uniqueFileName;

            // 6. 状态同步：更新 users 表中该用户的最新头像路径
            User updateUser = new User();
            updateUser.setId(userId);
            updateUser.setAvatarUrl(targetUrl);
            userMapper.updateById(updateUser);

            log.info("✅ 本地头像上传成功，物理路径: {}, 访问URL: {}", targetFile.getAbsolutePath(), targetUrl);

            return new UserAvatarVO(targetUrl);

        } catch (Exception e) {
            log.error("❌ 本地头像文件写入或数据库同步发生异常", e);
            throw new com.nav.exception.BaseException("头像上传失败，请检查服务器目录权限或磁盘空间");
        }
    }
}