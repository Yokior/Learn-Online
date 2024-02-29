package com.xuecheng.learning.service;

import com.xuecheng.base.model.RestResponse;
import org.springframework.stereotype.Service;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/29 19:58
 */
@Service
public interface LearningService
{
    RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId);
}
