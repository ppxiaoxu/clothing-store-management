package com.xinrong.store.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xinrong.store.dto.ChangePasswordRequest;
import com.xinrong.store.dto.LoginRequest;
import com.xinrong.store.dto.ProfileUpdateRequest;
import com.xinrong.store.dto.RegisterRequest;
import com.xinrong.store.dto.UserCreateRequest;
import com.xinrong.store.dto.UserResetPasswordRequest;
import com.xinrong.store.dto.UserStatusRequest;
import com.xinrong.store.dto.UserUpdateRequest;
import com.xinrong.store.entity.SysUser;
import com.xinrong.store.vo.LoginResponseVO;
import com.xinrong.store.vo.LoginUserVO;
import com.xinrong.store.vo.UserManageVO;

public interface SysUserService extends IService<SysUser> {

    LoginUserVO register(RegisterRequest request);

    LoginResponseVO login(LoginRequest request);

    LoginUserVO getLoginUser(Long userId);

    LoginUserVO updateProfile(Long userId, ProfileUpdateRequest request);

    void changePassword(Long userId, ChangePasswordRequest request);

    Page<UserManageVO> pageUsers(Integer pageNum, Integer pageSize, String keyword, String role, Integer status);

    UserManageVO createUser(UserCreateRequest request);

    UserManageVO updateUser(Long id, UserUpdateRequest request);

    void resetUserPassword(Long id, UserResetPasswordRequest request);

    void updateUserStatus(Long id, UserStatusRequest request, Long currentUserId);

    void deleteUser(Long id, Long currentUserId);
}
