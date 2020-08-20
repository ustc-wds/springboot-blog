package com.hyf.controller;

import com.hyf.bean.User;
import com.hyf.constant.CodeType;
import com.hyf.redis.StringRedisUtil;
import com.hyf.service.UserService;
import com.hyf.utils.JsonResult;
import com.hyf.utils.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author: zhangocean
 * @Date: 2018/6/8 9:24
 * Describe: 登录控制
 */
@RestController
public class LoginControl {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    private static final String SLASH_SYMBOL = "/";

    @Autowired
    UserService userService;
    @Autowired
    StringRedisUtil stringRedisUtil;
    @PostMapping(value = "/changePassword", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String changePassword(@RequestParam("email") String email,
                                 @RequestParam("authCode") String authCode,
                                 @RequestParam("newPassword") String newPassword){
        try {
            String trueMsgCode = (String) stringRedisUtil.get(email);

            //判断获得的手机号是否是发送验证码的手机号
            if(trueMsgCode == null){
                return JsonResult.fail(CodeType.EMAIL_ERROR).toJSON();
            }
            //判断验证码是否正确
            if(!authCode.equals(trueMsgCode)){
                return JsonResult.fail(CodeType.AUTH_CODE_ERROR).toJSON();
            }
            User user = userService.findUserByEmail(email);
            if(user == null){
                return JsonResult.fail(CodeType.USERNAME_NOT_EXIST).toJSON();
            }
            MD5Util md5Util = new MD5Util();
            String mD5Password = md5Util.encode(newPassword);
            userService.updatePasswordByEmail(email, mD5Password);

            //修改密码成功删除redis中的验证码
            stringRedisUtil.remove(email);

            return JsonResult.success().toJSON();
        } catch (Exception e){
            log.error("[{}] change password [{}] exception", email, newPassword, e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();
    }

}
