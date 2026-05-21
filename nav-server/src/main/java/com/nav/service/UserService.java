package com.nav.service;

import com.nav.dto.UserLoginDTO;
import com.nav.vo.UserLoginVO;
import com.nav.dto.UserRegisterDTO;
import com.nav.vo.UserVO;
public interface UserService {
    // 用户登录
    UserLoginVO login(UserLoginDTO userLoginDTO);
    // 用户注册
    void register(UserRegisterDTO userRegisterDTO);
    // 用户信息展示
    UserVO getCurrentInfo(Long userId);
}