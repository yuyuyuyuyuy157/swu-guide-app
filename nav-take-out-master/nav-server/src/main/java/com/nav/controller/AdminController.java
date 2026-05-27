package com.nav.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.github.pagehelper.PageHelper;
import com.nav.dto.AdminLoginDTO;
import com.nav.dto.AdminUserStatusUpdateDTO;
import com.nav.entity.User;
import com.nav.mapper.UserMapper;
import com.nav.result.PageResult;
import com.nav.result.Result;
import com.nav.service.UserService;
import com.nav.vo.AdminLoginVO;
import com.nav.vo.AdminUserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin APIs")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @PostMapping("/login")
    @Operation(summary = "Admin login")
    public Result<AdminLoginVO> login(@Validated @RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("Admin login request, account: {}", adminLoginDTO.getUsername());
        AdminLoginVO vo = userService.adminLogin(adminLoginDTO);
        return Result.success(vo);
    }

    @GetMapping("/users")
    @Operation(summary = "List users")
    public Result<PageResult<AdminUserVO>> listUsers(@RequestParam(required = false) String keyword,
                                                     @RequestParam(defaultValue = "1") Integer page,
                                                     @RequestParam(defaultValue = "20") Integer pageSize) {
        int safePage = page == null || page < 1 ? 1 : page;
        int safePageSize = pageSize == null || pageSize < 1 ? 20 : Math.min(pageSize, 50);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDeleted, 0);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(User::getPhone, keyword.trim());
        }
        wrapper.orderByDesc(User::getCreatedAt);

        com.github.pagehelper.Page<User> userPage = PageHelper.startPage(safePage, safePageSize);
        List<User> users = userMapper.selectList(wrapper);
        List<AdminUserVO> records = users.stream().map(this::toAdminUserVO).collect(Collectors.toList());
        return Result.success(new PageResult<>(userPage.getTotal(), records));
    }

    @PutMapping("/users/{id}/status")
    @Operation(summary = "Update user status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestBody AdminUserStatusUpdateDTO dto) {
        if (dto.getStatus() == null || dto.getStatus() < 1 || dto.getStatus() > 3) {
            return Result.error(400, "Status must be 1, 2, or 3");
        }

        User user = userMapper.selectById(id);
        if (user == null || (user.getIsDeleted() != null && user.getIsDeleted() == 1)) {
            return Result.error(404, "User not found");
        }

        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
        return Result.success(null);
    }

    private AdminUserVO toAdminUserVO(User user) {
        AdminUserVO vo = new AdminUserVO();
        vo.setUserId(user.getId().toString());
        vo.setPhone(maskPhone(user.getPhone()));
        vo.setAvatar(user.getAvatarUrl());
        vo.setRole(user.getRole());
        vo.setStatus(user.getStatus());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }

    private String maskPhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }
}
