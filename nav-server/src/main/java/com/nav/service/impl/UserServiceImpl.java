package com.nav.service.impl;

// =================== 1. 核心框架与事务依赖 ===================
import com.nav.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 🎯 核心：控制多表操作的事务回滚

// =================== 2. MyBatis-Plus 持久层组件 ===================
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

// =================== 3. 三方安全与工具包 (Hutool) ===================
import cn.hutool.crypto.SecureUtil; // 🎯 核心：用于进行加盐 MD5 加密

// =================== 4. 跨模块同胞组件依赖 (来自 common 和 pojo) ===================
import com.nav.constant.MessageConstant;
import com.nav.dto.UserLoginDTO;
import com.nav.dto.UserRegisterDTO;
import com.nav.entity.User;
import com.nav.exception.PhoneAlreadyExistsException;      // 🎯 核心：自定义手机号重复异常
import com.nav.exception.InvalidInvitationCodeException;  // 🎯 核心：自定义邀请码非法异常
import com.nav.mapper.UserMapper;
import com.nav.mapper.InvitationCodeMapper; // 🎯 核心：新引入的邀请码核销持久层
import com.nav.service.UserService;
import com.nav.vo.UserLoginVO;
import com.nav.vo.UserVO;
import com.nav.dto.UserEditPasswordDTO;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserService {
    //用户登录
    @Autowired
    private UserMapper userMapper;

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

        // 3. 核心： MD5 密码:为了防止 MD5 被轻松破解，我们将“明文密码 + 手机号(盐)”拼接后再进行 MD5
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
        java.util.Map<String, Object> claims = new java.util.HashMap<>();
        claims.put("userId", user.getId());
        String realToken = JwtUtil.createToken(claims);
        return UserLoginVO.builder()
                .token(realToken) // 返回真实 Token
                .userId(user.getId().toString())
                .phone(displayPhone)
                .avatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : "https://api.swu-guide-app.com/static/default-avatar.png")
                .role(user.getRole().toLowerCase())
                .playSettings(new HashMap<>())
                .build();

    }
    //用户注册
    @Autowired
    private InvitationCodeMapper invitationCodeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class) // 🎯 涉及两张表的操作，必须开启事务，保证原子性
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

        // 2. 预备构建新用户模型（提前用雪花算法算出 ID，以便反哺给邀请码表作外键关联）
        User newUser = new User();
        newUser.setPhone(phone);
        newUser.setRole("USER");
        newUser.setStatus(1); // 1-正常
        newUser.setIsDeleted(0);

        // 🔐 强力呼应：使用你选定的“明文密码 + 手机号”动态盐加盐方案进行 MD5 固化
        String encryptedPassword = cn.hutool.crypto.SecureUtil.md5(password + phone);
        newUser.setPasswordHash(encryptedPassword);

        // 插入用户表
        userMapper.insert(newUser);

        //使用原子更新核销邀请码
        // 如果受影响行数为 0，说明这个邀请码要么不存在，要么已经过期，要么刚刚在千分之一秒内被别人抢先用掉了！
        int rows = invitationCodeMapper.useCode(code, newUser.getId());
        if (rows == 0) {
            // 触发事务回滚，用户也不会被错误创建
            throw new InvalidInvitationCodeException(MessageConstant.INVALID_CODE);
        }
    }

    //用户信息展示
    @Override
    public UserVO getCurrentInfo(Long userId){
        User user=userMapper.selectById(userId);
        if(user==null||user.getIsDeleted()==1){
            throw new RuntimeException("用户不存在或已被注销");
        }

        String displayphone=user.getPhone().replaceAll("(]]d{3})\\d{4}(\\d{4})","$1****$2");

        return UserVO.builder()
                .userId(user.getId().toString())
                .phone(user.getPhone().toString())
                .avatar(user.getAvatarUrl() != null ? user.getAvatarUrl() : "https://api.swu-guide-app.com/static/default-avatar.png")
                .role(user.getRole())
                .build();
    }
    // 用户修改当前密码
    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);
    @Override
    @Transactional(rollbackFor = Exception.class) // 涉及核心凭证变更，开启事务
    public void updatePassword(UserEditPasswordDTO dto) {
        // 🎯 核心高阶操作：直接从当前线程的 ThreadLocal 中盲抠出被拦截器注入的当前登录用户 ID
        Long currentUserId = com.nav.context.BaseContext.getCurrentId();

        // 1. 根据 ID 捞出当前用户的完整实体（含手机号和原密码哈希）
        User user = userMapper.selectById(currentUserId);
        if (user == null || user.getIsDeleted() == 1) {
            throw new com.nav.exception.AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 🔐 校验旧密码：将输入的旧明文 + 手机号（动态盐）进行 MD5 加密
        String oldInputHash = cn.hutool.crypto.SecureUtil.md5(dto.getOldPassword() + user.getPhone());
        if (!oldInputHash.equals(user.getPasswordHash())) {
            throw new com.nav.exception.BaseException("原密码输入错误，请重新确认");
        }

        // 3. 🧠 哈希碰撞判定：防止用户将新密码设置得与原密码一模一样
        String newInputHash = cn.hutool.crypto.SecureUtil.md5(dto.getNewPassword() + user.getPhone());
        if (newInputHash.equals(user.getPasswordHash())) {
            throw new com.nav.exception.BaseException("新密码不能与原密码相同");
        }

        // 4. 更新密码与审计时效
        user.setPasswordHash(newInputHash);
        // 完美对应 nav.sql 中的核心字段，用于未来强制让该用户在其他端持有的存量过期 Token 失效
        user.setPasswordLastChangedAt(java.time.LocalDateTime.now());

        userMapper.updateById(user);
        log.info("🔒 用户 [ID: {}] 密码修改成功，密码时效审计点已更新。", currentUserId);
    }
}