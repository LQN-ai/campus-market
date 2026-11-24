package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.dto.LoginDTO;
import com.campus.market.dto.MerchantRegisterDTO;
import com.campus.market.dto.RegisterDTO;
import com.campus.market.entity.User;
import com.campus.market.security.UserContext;
import com.campus.market.service.AuthService;
import com.campus.market.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/captcha")
    public Result<Map<String, String>> captcha() {
        return Result.success(authService.getCaptcha());
    }

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @PostMapping("/merchant/register")
    public Result<Void> merchantRegister(@Valid @RequestBody MerchantRegisterDTO dto) {
        authService.merchantRegister(dto);
        return Result.success();
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    @GetMapping("/me")
    public Result<User> me() {
        return Result.success(authService.currentUser(UserContext.getUserId()));
    }
}
