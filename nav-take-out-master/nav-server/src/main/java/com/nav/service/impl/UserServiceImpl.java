package com.nav.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nav.constant.MessageConstant;
import com.nav.context.BaseContext;
import com.nav.dto.AdminLoginDTO;
import com.nav.dto.UserEditPasswordDTO;
import com.nav.dto.UserLoginDTO;
import com.nav.dto.UserRegisterDTO;
import com.nav.entity.User;
import com.nav.exception.AccountNotFoundException;
import com.nav.exception.BaseException;
import com.nav.exception.InvalidInvitationCodeException;
import com.nav.exception.PasswordErrorException;
import com.nav.exception.PhoneAlreadyExistsException;
import com.nav.mapper.InvitationCodeMapper;
import com.nav.mapper.UserMapper;
import com.nav.service.UserService;
import com.nav.utils.JwtUtil;
import com.nav.vo.AdminLoginVO;
import com.nav.vo.UserLoginVO;
import com.nav.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private InvitationCodeMapper invitationCodeMapper;

    @Override
    public UserLoginVO login(UserLoginDTO userLoginDTO) {
        String phone = userLoginDTO.getPhone();
        String password = userLoginDTO.getPassword();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone)
                .eq(User::getIsDeleted, 0)
                .last("LIMIT 1");
        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            if (user.getStatus() != null && user.getStatus() == 2) {
                throw new BaseException(MessageConstant.ACCOUNT_LOCKED);
            }
            throw new BaseException("账号不可用");
        }

        String inputHash = encodePassword(password, phone);
        if (!inputHash.equals(user.getPasswordHash())) {
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        String displayPhone = maskPhone(phone);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        String token = JwtUtil.createToken(claims);

        return UserLoginVO.builder()
                .token(token)
                .userId(user.getId().toString())
                .phone(displayPhone)
                .avatar(defaultAvatar(user.getAvatarUrl()))
                .role(user.getRole() == null ? "user" : user.getRole().toLowerCase())
                .playSettings(new HashMap<>())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserLoginVO register(UserRegisterDTO userRegisterDTO) {
        String phone = userRegisterDTO.getPhone();
        String password = userRegisterDTO.getPassword();
        String invitationCode = userRegisterDTO.getInvitationCode();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone)
                .eq(User::getIsDeleted, 0);
        Long count = userMapper.selectCount(wrapper);
        if (count != null && count > 0) {
            throw new PhoneAlreadyExistsException(MessageConstant.PHONE_EXISTS);
        }

        User user = new User();
        user.setPhone(phone);
        user.setRole("USER");
        user.setStatus(1);
        user.setIsDeleted(0);
        user.setPasswordHash(encodePassword(password, phone));
        userMapper.insert(user);

        int rows = invitationCodeMapper.useCode(invitationCode, user.getId());
        if (rows == 0) {
            throw new InvalidInvitationCodeException(MessageConstant.INVALID_CODE);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole());
        String token = JwtUtil.createToken(claims);

        return UserLoginVO.builder()
                .token(token)
                .userId(user.getId().toString())
                .phone(maskPhone(phone))
                .avatar(defaultAvatar(null))
                .role("user")
                .playSettings(new HashMap<>())
                .build();
    }

    @Override
    public UserVO getCurrentInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() == 1)) {
            throw new AccountNotFoundException("用户不存在");
        }
        return UserVO.builder()
                .userId(user.getId().toString())
                .phone(maskPhone(user.getPhone()))
                .avatar(defaultAvatar(user.getAvatarUrl()))
                .role(user.getRole())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(UserEditPasswordDTO dto) {
        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new BaseException("未登录或登录已失效");
        }

        User user = userMapper.selectById(currentUserId);
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() == 1)) {
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        String oldHash = encodePassword(dto.getOldPassword(), user.getPhone());
        if (!oldHash.equals(user.getPasswordHash())) {
            throw new PasswordErrorException("原密码错误");
        }

        String newHash = encodePassword(dto.getNewPassword(), user.getPhone());
        if (newHash.equals(user.getPasswordHash())) {
            throw new BaseException("新密码不能与原密码相同");
        }

        user.setPasswordHash(newHash);
        user.setPasswordLastChangedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("Password updated, userId={}", currentUserId);
    }

    @Override
    public AdminLoginVO adminLogin(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, username)
                .eq(User::getRole, "ADMIN")
                .eq(User::getIsDeleted, 0)
                .last("LIMIT 1");
        User admin = userMapper.selectOne(wrapper);

        if (admin == null) {
            throw new AccountNotFoundException("管理员账号不存在");
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new BaseException("管理员账号不可用");
        }

        String inputHash = encodePassword(password, username);
        if (!inputHash.equals(admin.getPasswordHash())) {
            throw new PasswordErrorException("管理员密码错误");
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", admin.getId());
        claims.put("role", "ADMIN");
        String token = JwtUtil.createToken(claims);

        return AdminLoginVO.builder()
                .token(token)
                .adminId(admin.getId().toString())
                .name("管理员(" + username + ")")
                .build();
    }

    private String encodePassword(String password, String phone) {
        return SecureUtil.md5(password + phone);
    }

    private String maskPhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    private String defaultAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return "https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg";
        }
        return avatarUrl;
    }
}
