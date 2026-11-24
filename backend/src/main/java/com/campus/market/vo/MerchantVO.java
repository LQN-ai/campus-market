package com.campus.market.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class MerchantVO {
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private Integer userStatus;
    private String shopName;
    private Integer level;
    private Integer shopStatus;
    private LocalDateTime banUntil;
    private BigDecimal rating;
    private BigDecimal totalSales;
    private String businessLicense;
    private String idCard;
}
