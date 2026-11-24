package com.campus.market.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("wallet_transactions")
public class WalletTransaction {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String type;
    private BigDecimal amount;
    private Long orderId;
    private String description;
    private LocalDateTime createdAt;
}
