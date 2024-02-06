package com.xuecheng.ucenter.impl;

import com.xuecheng.ucenter.AuthService;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import org.springframework.stereotype.Service;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/6 17:04
 */
@Service("password_authservice")
public class PasswordAuthServiceImpl implements AuthService
{
    @Override
    public XcUserExt execute(AuthParamsDto authParamsDto)
    {
        return null;
    }
}
