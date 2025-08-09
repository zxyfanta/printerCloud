package com.printercloud.controller;

import com.printercloud.common.R;
import com.printercloud.service.VerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/verification")
@Tag(name = "验证码(兼容)", description = "兼容小程序路径的验证码接口")
@RequiredArgsConstructor
public class VerificationAliasController {

    private final VerificationService verificationService;



    @PostMapping("/generate")
    @Operation(summary = "生成取件码")
    public R<String> generate(@RequestParam("orderId") Long orderId, @RequestAttribute("uid") Long uid) {
        return R.success("生成成功", verificationService.generatePickupCode(orderId, uid));
    }

    @PostMapping("/verify")
    @Operation(summary = "校验取件码")
    public R<Boolean> validate(@RequestParam("code") String code, @RequestAttribute("uid") Long uid) {
        boolean ok = verificationService.verifyPickupCode(code, uid);
        return ok ? R.success(true) : R.error("验证码无效或已过期");
    }
}
