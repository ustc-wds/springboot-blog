package com.hyf.aspect;


import com.hyf.aspect.annotation.PermissionCheck;
import com.hyf.constant.CodeType;
import com.hyf.utils.JsonResult;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * @author: zhangocean
 * @Date: 2019/11/1 13:21
 * Describe: 定义切面，拦截所有需要登录操作的controller接口
 */
@Component
public class PrincipalAspect {
    Logger log = LoggerFactory.getLogger(this.getClass());
    public static final String ANONYMOUS_USER = "anonymousUser";

    @Pointcut("execution(public * com.hyf.controller..*(..))")
    public void login(){

    }

    @Around("login() && @annotation(permissionCheck)")
    public Object principalAround(ProceedingJoinPoint pjp, PermissionCheck permissionCheck) throws Throwable {
        System.out.println("--------------------");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String loginName = auth.getName();
        //没有登录
        if(loginName.equals(ANONYMOUS_USER)){
            return JsonResult.fail(CodeType.USER_NOT_LOGIN).toJSON();
        }
        //接口权限拦截
        Collection<? extends GrantedAuthority> authority =  auth.getAuthorities();
        String value = permissionCheck.value();
        for(GrantedAuthority g : authority){
            if(g.getAuthority().equals(value)){
                return pjp.proceed();
            }
        }
        log.error("[{}] has no access to the [{}] method ", loginName, pjp.getSignature().getName());
        return JsonResult.fail(CodeType.PERMISSION_VERIFY_FAIL).toJSON();
    }

}
