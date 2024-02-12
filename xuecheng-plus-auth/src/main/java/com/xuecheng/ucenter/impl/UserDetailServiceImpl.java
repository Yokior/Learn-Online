package com.xuecheng.ucenter.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.AuthService;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
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

import java.util.List;
import java.util.stream.Collectors;

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
    private XcMenuMapper xcMenuMapper;

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

        if (StringUtils.isEmpty(authType))
        {
            throw new RuntimeException("认证类型不能为空");
        }

        // 从spring容器中获取对应的认证服务类
        String beanName = authType + "_authservice";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);

        // 调用统一认证方法
        XcUserExt xcUserExt = authService.execute(authParamsDto);

        // 封装成UserDetails对象给Security框架返回
        UserDetails userDetails = getUserDetails(xcUserExt);

        return userDetails;
    }

    private UserDetails getUserDetails(XcUserExt user)
    {
        String password = user.getPassword();
        user.setPassword(null);
        String userJson = JSON.toJSONString(user);
        // 根据id查询用户权限
        String authorities = null;
        List<XcMenu> xcMenuList = xcMenuMapper.selectPermissionByUserId(user.getId());
        if (xcMenuList.isEmpty())
        {
            throw new RuntimeException("用户权限不存在");
        }
        List<String> permissionList = xcMenuList.stream()
                .map(XcMenu::getCode)
                .collect(Collectors.toList());
        String[] permissionString = permissionList.toArray(new String[0]);

        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(permissionString).build();
        return userDetails;
    }
}
