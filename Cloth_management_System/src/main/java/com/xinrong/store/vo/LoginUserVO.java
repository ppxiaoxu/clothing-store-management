package com.xinrong.store.vo;

import com.xinrong.store.entity.SysUser;
import lombok.Data;

@Data
public class LoginUserVO {

    private Long id;

    private String username;

    private String nickname;

    private String role;

    public static LoginUserVO from(SysUser user) {
        LoginUserVO vo = new LoginUserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setRole(user.getRole());
        return vo;
    }
}
