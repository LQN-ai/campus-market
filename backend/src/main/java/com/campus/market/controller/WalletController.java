package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.entity.Wallet;
import com.campus.market.entity.WalletTransaction;
import com.campus.market.security.UserContext;
import com.campus.market.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {

    private final WalletService walletService;

    @GetMapping
    public Result<Wallet> wallet() {
        return Result.success(walletService.getOrCreate(UserContext.getUserId()));
    }

    @GetMapping("/transactions")
    public Result<List<WalletTransaction>> transactions() {
        return Result.success(walletService.transactions(UserContext.getUserId()));
    }

    @PostMapping("/redeem")
    public Result<Void> redeem(@RequestBody Map<String, Integer> body) {
        walletService.redeem(UserContext.getUserId(), body.getOrDefault("points", 0));
        return Result.success();
    }
}
