package com.nav.service;

import com.nav.dto.UserLoginDTO;
import com.nav.vo.*;
import com.nav.dto.UserRegisterDTO;
import com.nav.dto.AdminLoginDTO;
import com.nav.dto.UserEditPasswordDTO;
import com.nav.dto.UserAudioSettingDTO;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    // 用户登录
    UserLoginVO login(UserLoginDTO userLoginDTO);
    //管理员登录
    AdminLoginVO adminLogin(AdminLoginDTO adminLoginDTO);
    // 用户注册
    void register(UserRegisterDTO userRegisterDTO);
    // 用户信息展示
    UserVO getCurrentInfo(Long userId);
    // 用户修改当前密码
    void changePassword(UserEditPasswordDTO dto);
    // 用户修改音频设置
    void updateAudioSetting(Long userId, UserAudioSettingDTO userAudioSettingDTO);
    // 用户获取当前音频设置
    UserAudioSettingVO getAudioSetting(Long userId);
    // 用户上传头像
    UserAvatarVO uploadAvatar(Long userId, MultipartFile file);
}