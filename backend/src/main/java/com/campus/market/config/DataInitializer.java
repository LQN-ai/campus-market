package com.campus.market.config;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.market.entity.User;
import com.campus.market.entity.Wallet;
import com.campus.market.mapper.UserMapper;
import com.campus.market.mapper.WalletMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 启动时初始化管理员账号
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final WalletMapper walletMapper;

    @Override
    public void run(String... args) {
        Long adminCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "ADMIN"));
        if (adminCount == 0) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(BCrypt.hashpw("admin123"));
            admin.setRealName("系统管理员");
            admin.setRole("ADMIN");
            admin.setStatus(1);
            userMapper.insert(admin);

            Wallet w = new Wallet();
            w.setUserId(admin.getId());
            w.setBalance(BigDecimal.ZERO);
            w.setPoints(0);
            walletMapper.insert(w);
            log.info("已初始化管理员账号：admin / admin123");
        }
    }
}
