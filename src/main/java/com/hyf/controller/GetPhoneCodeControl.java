package com.hyf.controller;

import com.hyf.constant.ClientException;
import com.hyf.redis.StringRedisUtil;
import com.hyf.utils.JsonResult;
import com.hyf.utils.VerificationCodeGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author: zhangocean
 * @Date: 2018/6/4 15:03
 * Describe: 注册获得手机验证码
 */
@RestController
public class GetPhoneCodeControl {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String REGISTER = "register";

    @Autowired
    private StringRedisUtil stringRedisUtil;

    @PostMapping(value = "/getCode", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getAuthCode(@RequestParam("phone") String phone,
                              @RequestParam("sign") String sign){

        String trueMsgCode = VerificationCodeGenerator.randomBuilder();

       //在redis中保存手机号验证码并设置过期时间
        stringRedisUtil.set(phone, trueMsgCode);
        stringRedisUtil.expire(phone, 300);

        String msgCode;
        //注册的短信模板
        if(REGISTER.equals(sign)){
            msgCode = "SMS_136394413";
        }
        //改密码的短信模板
        else {
            msgCode = "SMS_139982667";
        }

        try {
            sendSmsResponse(phone, trueMsgCode, msgCode);
        } catch (ClientException e) {
            log.error("[{}] send phone message exception" + phone + e);
            return JsonResult.fail().toJSON();
        }

        return JsonResult.success().toJSON();
    }

    private void sendSmsResponse(String phone, String trueMsgCode, String msgCode) throws ClientException{
    }
}
