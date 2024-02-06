package com.xuecheng.ucenter.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.AuthService;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.po.XcUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/6 15:26
 */
@Component
@Slf4j
public class UserDetailServiceImpl implements UserDetailsService
{

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private ApplicationContext applicationContext;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException
    {
        if (StringUtils.isEmpty(s))
        {
            return null;
        }

        AuthParamsDto authParamsDto = null;

        try
        {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }
        catch (Exception e)
        {
            throw new RuntimeException("请求认证参数格式错误");
        }

        // 根据认证类型调用不同的实现类
        String authType = authParamsDto.getAuthType();
        // 从spring容器中获取对应的认证服务类
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);

        // 调用统一认证方法
        authService.execute(authParamsDto);


        String username = authParamsDto.getUsername();
        if (StringUtils.isEmpty(username))
        {
            return null;
        }
        // 根据username查询数据库
        LambdaQueryWrapper<XcUser> lqw = new LambdaQueryWrapper<>();
        lqw.eq(XcUser::getUsername, username);
        XcUser xcUser = xcUserMapper.selectOne(lqw);

        // 用户不存在返回null 让之后的过滤器抛出异常
        if (xcUser == null)
        {
            return null;
        }

        // 拿到正确的密码 封装成UserDetails对象给Security框架返回
        String password = xcUser.getPassword();
        xcUser.setPassword(null);
        String authorities = JSON.toJSONString(xcUser);
        UserDetails userDetails = User.withUsername(authorities).password(password).authorities(authorities).build();

        return userDetails;
    }
}
