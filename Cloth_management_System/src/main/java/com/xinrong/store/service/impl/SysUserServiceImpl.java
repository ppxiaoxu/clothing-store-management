package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinrong.store.dto.ChangePasswordRequest;
import com.xinrong.store.dto.LoginRequest;
import com.xinrong.store.dto.ProfileUpdateRequest;
import com.xinrong.store.dto.RegisterRequest;
import com.xinrong.store.dto.UserCreateRequest;
import com.xinrong.store.dto.UserResetPasswordRequest;
import com.xinrong.store.dto.UserStatusRequest;
import com.xinrong.store.dto.UserUpdateRequest;
import com.xinrong.store.entity.SysUser;
import com.xinrong.store.mapper.SysUserMapper;
import com.xinrong.store.service.SysUserService;
import com.xinrong.store.util.JwtUtil;
import com.xinrong.store.vo.LoginResponseVO;
import com.xinrong.store.vo.LoginUserVO;
import com.xinrong.store.vo.UserManageVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final String DEFAULT_ROLE = "STORE_OWNER";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SysUserServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginUserVO register(RegisterRequest request) {
        throw new IllegalArgumentException("注册功能已关闭，请联系管理员创建账号");
    }

    @Override
    public LoginResponseVO login(LoginRequest request) {
        SysUser user = this.getOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, request.getUsername().trim())
                .last("limit 1"));

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (!Integer.valueOf(STATUS_ENABLED).equals(user.getStatus())) {
            throw new IllegalArgumentException("用户已被禁用");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return new LoginResponseVO(token, LoginUserVO.from(user));
    }

    @Override
    public LoginUserVO getLoginUser(Long userId) {
        return LoginUserVO.from(getRequiredUser(userId));
    }

    @Override
    public LoginUserVO updateProfile(Long userId, ProfileUpdateRequest request) {
        SysUser user = getRequiredUser(userId);
        user.setNickname(request.getNickname().trim());
        user.setPhone(trimOrEmpty(request.getPhone()));
        user.setAvatar(trimOrEmpty(request.getAvatar()));
        this.updateById(user);
        return LoginUserVO.from(user);
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        SysUser user = getRequiredUser(userId);
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("旧密码不正确");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("两次输入的新密码不一致");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("新密码不能与旧密码相同");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        this.updateById(user);
    }

    @Override
    public Page<UserManageVO> pageUsers(Integer pageNum, Integer pageSize, String keyword, String role, Integer status) {
        Page<SysUser> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            String value = keyword.trim();
            wrapper.and(w -> w.like(SysUser::getUsername, value)
                    .or()
                    .like(SysUser::getNickname, value)
                    .or()
                    .like(SysUser::getPhone, value));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(SysUser::getRole, normalizeRole(role));
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, validateStatus(status));
        }
        wrapper.orderByDesc(SysUser::getCreateTime).orderByDesc(SysUser::getId);

        Page<SysUser> userPage = this.page(page, wrapper);
        Page<UserManageVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        voPage.setRecords(userPage.getRecords().stream().map(UserManageVO::from).toList());
        return voPage;
    }

    @Override
    public UserManageVO createUser(UserCreateRequest request) {
        String username = request.getUsername().trim();
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname().trim());
        user.setPhone(trimOrEmpty(request.getPhone()));
        user.setRole(normalizeRole(request.getRole()));
        user.setStatus(validateStatus(request.getStatus()));
        this.save(user);
        return UserManageVO.from(user);
    }

    @Override
    public UserManageVO updateUser(Long id, UserUpdateRequest request) {
        SysUser user = getRequiredUser(id);
        user.setNickname(request.getNickname().trim());
        user.setPhone(trimOrEmpty(request.getPhone()));
        user.setRole(normalizeRole(request.getRole()));
        user.setStatus(validateStatus(request.getStatus()));
        this.updateById(user);
        return UserManageVO.from(user);
    }

    @Override
    public void resetUserPassword(Long id, UserResetPasswordRequest request) {
        SysUser user = getRequiredUser(id);
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        this.updateById(user);
    }

    @Override
    public void updateUserStatus(Long id, UserStatusRequest request, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("不能修改自己的账号状态");
        }
        SysUser user = getRequiredUser(id);
        user.setStatus(validateStatus(request.getStatus()));
        this.updateById(user);
    }

    @Override
    public void deleteUser(Long id, Long currentUserId) {
        if (id.equals(currentUserId)) {
            throw new IllegalArgumentException("不能删除当前登录用户");
        }
        if (!this.removeById(id)) {
            throw new IllegalArgumentException("用户不存在");
        }
    }

    private boolean existsByUsername(String username) {
        return this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) > 0;
    }

    private SysUser getRequiredUser(Long id) {
        SysUser user = this.getById(id);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return user;
    }

    private String normalizeRole(String role) {
        String value = role == null ? "" : role.trim().toUpperCase();
        if (ADMIN_ROLE.equals(value) || DEFAULT_ROLE.equals(value)) {
            return value;
        }
        throw new IllegalArgumentException("角色不正确");
    }

    private Integer validateStatus(Integer status) {
        if (Integer.valueOf(STATUS_ENABLED).equals(status) || Integer.valueOf(STATUS_DISABLED).equals(status)) {
            return status;
        }
        throw new IllegalArgumentException("状态不正确");
    }

    private String trimOrEmpty(String value) {
        return StringUtils.hasText(value) ? value.trim() : "";
    }
}
