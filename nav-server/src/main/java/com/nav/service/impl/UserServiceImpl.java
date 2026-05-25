package com.nav.service.impl;

//核心框架与事务依赖
import com.nav.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 控制多表操作的事务回滚

//MyBatis-Plus 持久层组件
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

//三方安全与工具包
import cn.hutool.crypto.SecureUtil; // 用于进行加盐 MD5 加密

// 跨模块同胞组件依赖 (来自 common 和 pojo)
import com.nav.constant.MessageConstant;
import com.nav.dto.UserLoginDTO;
import com.nav.dto.UserRegisterDTO;
import com.nav.entity.User;
import com.nav.exception.PhoneAlreadyExistsException;      // 自定义手机号重复异常
import com.nav.exception.InvalidInvitationCodeException;  // 自定义邀请码非法异常
import com.nav.mapper.UserMapper;
import com.nav.mapper.InvitationCodeMapper; //新引入的邀请码核销持久层
import com.nav.service.UserService;
import com.nav.vo.UserLoginVO;
import com.nav.vo.UserVO;
import com.nav.dto.UserEditPasswordDTO;
import com.nav.properties.JwtProperties; // 企业级配置解耦属性类
import com.nav.vo.AdminLoginVO;
import com.nav.dto.AdminLoginDTO;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InvitationCodeMapper invitationCodeMapper;

    @Autowired
    private JwtProperties jwtProperties; // 读取 yml 文件的动态参数

    /**
     * 用户端 - 密码登录
     */
    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        String password = userLoginDTO.getPassword();

        // 1. 根据手机号查询未删除的用户
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(queryWrapper);

        // 2. 账号存在性校验
        if (user == null) {
            throw new RuntimeException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 3. 核心：为了防止 MD5 被轻松破解，我们将“明文密码 + 手机号(盐)”拼接后再进行 MD5
        String saltPassword = password + phone;
        String inputHash = SecureUtil.md5(saltPassword); // 计算出输入的密文

        // 拿计算出的密文与数据库中的 password_hash 进行比对
        if (!inputHash.equals(user.getPasswordHash())) {
            throw new RuntimeException(MessageConstant.PASSWORD_ERROR);
        }

        // 4. 账号状态校验
        if (user.getStatus() == 2) { // 2-冻结
            throw new RuntimeException(MessageConstant.ACCOUNT_LOCKED);
        }

        // 5. 数据脱敏与组装返回
        String displayPhone = phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());

        // 返回token
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

        // 1. 唯一性校验：检查该手机号是否在 users 表中已存在
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        Long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new PhoneAlreadyExistsException(MessageConstant.PHONE_EXISTS);
        }

        // 2. 预备构建新用户模型
        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setRole("USER");
        newUser.setStatus(1); // 1-正常
        newUser.setIsDeleted(0);

        String encryptedPassword = SecureUtil.md5(password + phone);
        newUser.setPasswordHash(encryptedPassword);

        // 插入用户表
        userMapper.insert(newUser);

        // 使用原子更新核销邀请码
        int rows = invitationCodeMapper.useCode(code, newUser.getId());
        if (rows == 0) {
            throw new InvalidInvitationCodeException(MessageConstant.INVALID_CODE);
        }
    }

    /**
     * 用户端 - 用户信息展示
     */
    @Override
    public UserVO getCurrentInfo(Long userId){
        User user = userMapper.selectById(userId);
        if(user == null || user.getIsDeleted() == 1){
            throw new RuntimeException("用户不存在或已被注销");
        }

        String displayPhone = user.getPhone().replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");

        return UserVO.builder()
                .userId(user.getId().toString())
                .phone(displayPhone)
                .avatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : "https://api.swu-guide-app.com/static/default-avatar.png")
                .role(user.getRole())
                .build();
    }

    /**
     * 用户端 - 修改当前密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(UserEditPasswordDTO dto) {
        Long currentUserId = com.nav.context.BaseContext.getCurrentId();

        // 1. 根据 ID 捞出当前用户的完整实体
        User user = userMapper.selectById(currentUserId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new com.nav.exception.AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 校验旧密码
        String oldInputHash = SecureUtil.md5(dto.getOldPassword() + user.getPhone());
        if (!oldInputHash.equals(user.getPasswordHash())) {
            throw new com.nav.exception.BaseException("原密码输入错误，请重新确认");
        }

        // 3. 哈希碰撞判定
        String newInputHash = SecureUtil.md5(dto.getNewPassword() + user.getPhone());
        if (newInputHash.equals(user.getPasswordHash())) {
            throw new com.nav.exception.BaseException("新密码不能与原密码相同");
        }

        // 4. 更新密码与审计时效
        user.setPasswordHash(newInputHash);
        user.setPasswordLastChangedAt(java.time.LocalDateTime.now());

        userMapper.updateById(user);
        log.info("用户 [ID: {}] 密码修改成功，密码时效审计点已更新。", currentUserId);
    }

    /**
     * 管理端 - 管理员登录
     */
    @Override
    public AdminLoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();

        // 1. 根据账号过滤
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, username)
                .eq(User::getRole, "ADMIN");
        User admin = userMapper.selectOne(queryWrapper);

        if (admin == null) {
            throw new com.nav.exception.BaseException("管理员账号不存在");
        }

        // 2. 🔐 密码比对：完全沿用你选定的加盐 MD5 算法
        String inputHash = SecureUtil.md5(password + username);
        if (!inputHash.equals(admin.getPasswordHash())) {
            throw new com.nav.exception.BaseException("管理员密码错误");
        }

        // 3. 🎯 核心变动：生成管理员专属 Token（利用重载方法，动态传入安全参数）
        Map<String, Object> claims = new HashMap<>();
        // 统一键名为 "userId"，方便统一鉴权拦截器提取（你的拦截器里读的是 decodedJWT.getClaim("userId")）
        claims.put("userId", admin.getId());
        claims.put("role", "ADMIN");

        // 🎯 借力升级：调用高阶重载，确保管理端 24 小时绝对安全离线
        String token = JwtUtil.createToken(
                claims,
                jwtProperties.getAdminTtl(),
                jwtProperties.getAdminSecretKey()
        );

        return AdminLoginVO.builder()
                .token(token)
                .adminId(admin.getId().toString())
                .name("高级管理员(" + username.substring(0, 3) + ")") // 模拟审计名
                .build();
    }
}