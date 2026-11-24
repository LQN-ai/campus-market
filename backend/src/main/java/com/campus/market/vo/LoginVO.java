package com.campus.market.vo;

import com.campus.market.entity.User;
import lombok.Data;

@Data
public class LoginVO {
    private String token;
    private User user;
}
