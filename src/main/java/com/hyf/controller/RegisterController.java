package com.hyf.controller;

import com.hyf.aspect.PrincipalAspect;
import com.hyf.bean.User;
import com.hyf.constant.CodeType;
import com.hyf.redis.StringRedisUtil;
import com.hyf.service.UserService;
import com.hyf.utils.DataMap;
import com.hyf.utils.JsonResult;
import com.hyf.utils.MD5Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class RegisterController {

    Logger log = LoggerFactory.getLogger(RegisterController.class);
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisUtil stringRedisUtil;

    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String register(User user, HttpServletRequest request){
        try {
            String authCode = request.getParameter("authCode");

            String trueMsgCode = (String) stringRedisUtil.get(user.getEmail());

            //判断手机号是否正确
            if(trueMsgCode == null){
                return JsonResult.fail(CodeType.EMAIL_ERROR).toJSON();
            }
            //判断验证码是否正确
            if(!authCode.equals(trueMsgCode)){
                return JsonResult.fail(CodeType.AUTH_CODE_ERROR).toJSON();
            }
            //判断用户名是否存在
            if(userService.usernameIsExist(user.getUsername()) || user.getUsername().equals(PrincipalAspect.ANONYMOUS_USER)){
                return JsonResult.fail(CodeType.USERNAME_EXIST).toJSON();
            }
            //注册时对密码进行MD5加密
            MD5Util md5Util = new MD5Util();
            user.setPassword(md5Util.encode(user.getPassword()));

            //注册结果
            DataMap data = userService.insert(user);
            if (0 == data.getCode()){
                //注册成功删除redis中的验证码
                stringRedisUtil.remove(user.getEmail());
            }
            return JsonResult.build(data).toJSON();
        } catch (Exception e){
            log.error("User [{}] register exception", user, e);
        }
        return JsonResult.fail(CodeType.SERVER_EXCEPTION).toJSON();
    }

}
