package com.xinrong.store.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xinrong.store.dto.LoginRequest;
import com.xinrong.store.dto.RegisterRequest;
import com.xinrong.store.entity.SysUser;
import com.xinrong.store.mapper.SysUserMapper;
import com.xinrong.store.service.SysUserService;
import com.xinrong.store.util.JwtUtil;
import com.xinrong.store.vo.LoginResponseVO;
import com.xinrong.store.vo.LoginUserVO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private static final String DEFAULT_ROLE = "STORE_OWNER";
    private static final int STATUS_ENABLED = 1;

    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public SysUserServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginUserVO register(RegisterRequest request) {
        String username = request.getUsername().trim();
        if (existsByUsername(username)) {
            throw new IllegalArgumentException("用户名已存在");
        }

        SysUser user = new SysUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? request.getNickname().trim() : username);
        user.setRole(DEFAULT_ROLE);
        user.setStatus(STATUS_ENABLED);
        this.save(user);
        return LoginUserVO.from(user);
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
        SysUser user = this.getById(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在");
        }
        return LoginUserVO.from(user);
    }

    private boolean existsByUsername(String username) {
        return this.count(new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) > 0;
    }
}
