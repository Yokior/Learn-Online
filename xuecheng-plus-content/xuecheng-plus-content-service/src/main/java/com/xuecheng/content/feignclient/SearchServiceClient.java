package com.xuecheng.content.feignclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/3 15:27
 */
@FeignClient(value = "search", fallbackFactory =  SearchServiceClientFallbackFactory.class)
public interface SearchServiceClient
{
    @PostMapping("/search/index/course")
    Boolean add(@RequestBody CourseIndex courseIndex);
}
