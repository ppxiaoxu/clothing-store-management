package com.xinrong.store.controller;

import com.xinrong.store.common.Result;
import com.xinrong.store.dto.ChangePasswordRequest;
import com.xinrong.store.dto.LoginRequest;
import com.xinrong.store.dto.ProfileUpdateRequest;
import com.xinrong.store.dto.RegisterRequest;
import com.xinrong.store.service.SysUserService;
import com.xinrong.store.vo.LoginResponseVO;
import com.xinrong.store.vo.LoginUserVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final SysUserService sysUserService;

    public AuthController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @PostMapping("/register")
    public Result<LoginUserVO> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success(sysUserService.register(request));
    }

    @PostMapping("/login")
    public Result<LoginResponseVO> login(@Valid @RequestBody LoginRequest request) {
        return Result.success(sysUserService.login(request));
    }

    @GetMapping("/me")
    public Result<LoginUserVO> me(@RequestAttribute("userId") Long userId) {
        return Result.success(sysUserService.getLoginUser(userId));
    }

    @PutMapping("/profile")
    public Result<LoginUserVO> updateProfile(@RequestAttribute("userId") Long userId,
                                             @Valid @RequestBody ProfileUpdateRequest request) {
        return Result.success(sysUserService.updateProfile(userId, request));
    }

    @PutMapping("/change-password")
    public Result<Void> changePassword(@RequestAttribute("userId") Long userId,
                                       @Valid @RequestBody ChangePasswordRequest request) {
        sysUserService.changePassword(userId, request);
        return Result.success();
    }
}
