package com.xuecheng.auth.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * @Description：有些认证方式不需要密码，所以需要自定义认证方式
 * @Auther：Yokior
 * @Date：2024/2/6 16:47
 */
@Component
public class DaoAuthenticationProviderCustom extends DaoAuthenticationProvider
{

    @Autowired
    @Override
    public void setUserDetailsService(UserDetailsService userDetailsService)
    {
        super.setUserDetailsService(userDetailsService);
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException
    {
        // 可能不需要密码 例如手机验证码登录 原方法会判断密码不正确直接异常
        // 这里直接重写覆盖原方法 置空 至于详细的验证方法在AuthService中实现
    }
}
