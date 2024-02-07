package com.xuecheng.ucenter.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.AuthService;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/6 17:04
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService
{

    @Autowired
    private XcUserMapper xcUserMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto)
    {

        // TODO: 校验验证码

        // 获取用户名
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
            throw new RuntimeException("账号不存在");
        }

        String dbPassword = xcUser.getPassword();
        String inputPassword = authParamsDto.getPassword();
        // 校验密码
        boolean matches = passwordEncoder.matches(inputPassword, dbPassword);

        if (!matches)
        {
            throw new RuntimeException("账号或密码错误");
        }

        XcUserExt xcUserExt = new XcUserExt();
        BeanUtils.copyProperties(xcUser,xcUserExt);

        return xcUserExt;
    }
}
