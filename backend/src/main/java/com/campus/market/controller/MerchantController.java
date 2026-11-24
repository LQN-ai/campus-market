package com.campus.market.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.Result;
import com.campus.market.entity.MerchantProfile;
import com.campus.market.mapper.MerchantProfileMapper;
import com.campus.market.security.RequireRole;
import com.campus.market.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@RequireRole("MERCHANT")
public class MerchantController {

    private final MerchantProfileMapper merchantProfileMapper;

    /**
     * 获取当前商家的店铺信息（店铺名、等级、好评率、累计交易额等）
     */
    @GetMapping("/profile")
    public Result<MerchantProfile> profile() {
        MerchantProfile p = merchantProfileMapper.selectOne(
                new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, UserContext.getUserId()));
        return Result.success(p);
    }
}
