package com.xuecheng.ucenter.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/8 15:54
 */
@Slf4j
@Component
public class CheckCodeClientFactory implements FallbackFactory<CheckCodeClient>
{
    @Override
    public CheckCodeClient create(Throwable throwable)
    {
        return new CheckCodeClient()
        {
            @Override
            public Boolean verify(String key, String code)
            {
                log.error("调用远程校验验证码服务失败:{}", throwable.getMessage());
                return null;
            }
        };
    }
}
