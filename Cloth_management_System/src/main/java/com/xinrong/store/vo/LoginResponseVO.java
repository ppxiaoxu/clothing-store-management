package com.xinrong.store.vo;

import lombok.Data;

@Data
public class LoginResponseVO {

    private String token;

    private LoginUserVO user;

    public LoginResponseVO(String token, LoginUserVO user) {
        this.token = token;
        this.user = user;
    }
}
