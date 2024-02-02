package com.xuecheng.content.feignclient;

import org.springframework.web.multipart.MultipartFile;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/2 14:54
 */
public class MediaServiceClientFallback implements MediaServiceClient
{
    @Override
    public String upload(MultipartFile filedata)
    {
        // 这种方法无法拿到熔断异常


        return null;
    }
}
