package com.campus.market.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.common.Result;
import com.campus.market.entity.Blacklist;
import com.campus.market.entity.MerchantProfile;
import com.campus.market.mapper.BlacklistMapper;
import com.campus.market.mapper.MerchantProfileMapper;
import com.campus.market.security.RequireRole;
import com.campus.market.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@RequireRole("MERCHANT")
public class MerchantController {

    private final MerchantProfileMapper merchantProfileMapper;
    private final BlacklistMapper blacklistMapper;

    /**
     * 获取当前商家的店铺信息（店铺名、等级、好评率、累计交易额等）
     */
    @GetMapping("/profile")
    public Result<MerchantProfile> profile() {
        MerchantProfile p = merchantProfileMapper.selectOne(
                new LambdaQueryWrapper<MerchantProfile>().eq(MerchantProfile::getUserId, UserContext.getUserId()));
        return Result.success(p);
    }

    /**
     * 商家拉黑买家（买家将无法购买该商家的商品）
     */
    @PostMapping("/blacklist")
    public Result<Void> blacklist(@RequestBody Map<String, Long> body) {
        Long buyerId = body.get("buyerId");
        Long merchantId = UserContext.getUserId();
        Long exist = blacklistMapper.selectCount(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getUserId, buyerId)
                .eq(Blacklist::getMerchantId, merchantId));
        if (exist == 0) {
            Blacklist b = new Blacklist();
            b.setUserId(buyerId);
            b.setMerchantId(merchantId);
            blacklistMapper.insert(b);
        }
        return Result.success();
    }

    /**
     * 商家解除拉黑
     */
    @PostMapping("/unblacklist")
    public Result<Void> unblacklist(@RequestBody Map<String, Long> body) {
        Long buyerId = body.get("buyerId");
        Long merchantId = UserContext.getUserId();
        blacklistMapper.delete(new LambdaQueryWrapper<Blacklist>()
                .eq(Blacklist::getUserId, buyerId)
                .eq(Blacklist::getMerchantId, merchantId));
        return Result.success();
    }
}
