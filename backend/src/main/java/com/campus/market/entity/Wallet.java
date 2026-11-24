package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

@Data
@TableName("wallets")
public class Wallet {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private BigDecimal balance;
    private Integer points;
}
