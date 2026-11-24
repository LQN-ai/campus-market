package com.campus.market.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 商家注册
 */
@Data
public class MerchantRegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, message = "密码至少6位")
    private String password;

    private String realName;
    private String phone;
    private Integer gender;

    @Size(min = 16, max = 16, message = "银行账号必须为16位")
    private String bankAccount;

    @NotBlank(message = "店铺名不能为空")
    private String shopName;

    private String businessLicense;
    private String idCard;

    @NotBlank(message = "验证码ID不能为空")
    private String captchaId;

    @NotBlank(message = "验证码不能为空")
    private String captchaCode;
}
