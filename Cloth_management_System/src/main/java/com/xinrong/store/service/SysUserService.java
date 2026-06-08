package com.xinrong.store.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xinrong.store.dto.ChangePasswordRequest;
import com.xinrong.store.dto.LoginRequest;
import com.xinrong.store.dto.RegisterRequest;
import com.xinrong.store.entity.SysUser;
import com.xinrong.store.vo.LoginResponseVO;
import com.xinrong.store.vo.LoginUserVO;

public interface SysUserService extends IService<SysUser> {

    LoginUserVO register(RegisterRequest request);

    LoginResponseVO login(LoginRequest request);

    LoginUserVO getLoginUser(Long userId);

    void changePassword(Long userId, ChangePasswordRequest request);
}
