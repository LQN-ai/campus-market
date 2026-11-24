package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchant_profiles")
public class MerchantProfile {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String shopName;
    private String businessLicense;
    private String idCard;
    private Integer level;
    private Integer shopStatus;
    private LocalDateTime banUntil;
    private BigDecimal rating;
    private BigDecimal totalSales;
}
