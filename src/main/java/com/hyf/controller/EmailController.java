package com.hyf.controller;

import com.hyf.redis.StringRedisUtil;
import com.hyf.service.MailService;
import com.hyf.utils.JsonResult;
import com.hyf.utils.VerificationCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.awt.*;

@RestController
@RequestMapping("/email")
public class EmailController {

    @Autowired
    private MailService mailService;

    @Autowired
    private StringRedisUtil stringRedisUtil;

    @RequestMapping(value = "/SimpleEmail",produces = MediaType.APPLICATION_JSON_VALUE)
    public String sendSimpleEmail(@RequestParam("email") String toEmailAddress) {
        String verificationCode = VerificationCodeGenerator.randomBuilder();
        stringRedisUtil.set(toEmailAddress,verificationCode,300);
        //mailService.sendSimpleMail(to,"验证码", VerificationCodeGenerator.randomBuilder());
        mailService.sendSimpleMail(toEmailAddress,"验证码邮件",verificationCode);
        return JsonResult.success().toJSON();
    }

}
