package com.xuecheng.content.feignclient;

import com.xuecheng.content.config.MultipartSupportConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/2 14:33
 */
@FeignClient(value = "media-api", configuration = {MultipartSupportConfig.class}, fallbackFactory = MediaServiceClientFallbackFactory.class)
public interface MediaServiceClient
{
    @PostMapping("/media/upload/coursefile")
    String upload(@RequestParam("filedata") MultipartFile filedata);

}
