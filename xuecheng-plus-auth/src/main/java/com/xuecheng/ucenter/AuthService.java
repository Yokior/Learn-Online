package com.xuecheng.ucenter;

import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;

/**
 * @Description：统一认证接口
 * @Auther：Yokior
 * @Date：2024/2/6 17:02
 */
public interface AuthService
{

    XcUserExt execute(AuthParamsDto authParamsDto);

}
