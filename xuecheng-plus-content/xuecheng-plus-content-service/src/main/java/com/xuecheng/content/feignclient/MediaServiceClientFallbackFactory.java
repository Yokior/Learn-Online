package com.xuecheng.content.feignclient;

import feign.hystrix.FallbackFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/2 14:58
 */
@Slf4j
@Component
public class MediaServiceClientFallbackFactory implements FallbackFactory<MediaServiceClient>
{
    @Override
    public MediaServiceClient create(Throwable throwable)
    {
        return new MediaServiceClient()
        {
            @Override
            public String upload(MultipartFile filedata)
            {
                log.error("远程调用上传文件接口发生熔断:{}", throwable.toString());
                return null;
            }
        };
    }
}
