package com.nav.service;

import com.nav.dto.UserLoginDTO;
import com.nav.vo.UserLoginVO;
import com.nav.dto.UserRegisterDTO;
import com.nav.vo.UserVO;
import com.nav.dto.AdminLoginDTO;
import com.nav.vo.AdminLoginVO;
import com.nav.dto.UserEditPasswordDTO;
public interface UserService {
    // 用户登录
    UserLoginVO login(UserLoginDTO userLoginDTO);
    //管理员登录
    public AdminLoginVO adminLogin(AdminLoginDTO adminLoginDTO);
    // 用户注册
    void register(UserRegisterDTO userRegisterDTO);
    // 用户信息展示
    UserVO getCurrentInfo(Long userId);
    // 用户修改当前密码
    void changePassword(UserEditPasswordDTO dto);
}