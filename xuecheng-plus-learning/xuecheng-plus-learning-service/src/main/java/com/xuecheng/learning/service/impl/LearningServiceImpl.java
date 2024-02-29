package com.xuecheng.learning.service.impl;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.content.model.entity.CoursePublish;
import com.xuecheng.learning.feignclient.ContentServiceClient;
import com.xuecheng.learning.feignclient.MediaServiceClient;
import com.xuecheng.learning.model.dto.XcCourseTablesDto;
import com.xuecheng.learning.service.LearningService;
import com.xuecheng.learning.service.MyCourseTablesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/29 20:04
 */
public class LearningServiceImpl implements LearningService
{

    @Autowired
    private MyCourseTablesService myCourseTablesService;

    @Autowired
    private ContentServiceClient contentServiceClient;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Override
    public RestResponse<String> getVideo(String userId, Long courseId, Long teachplanId, String mediaId)
    {
        // 查询课程信息
        CoursePublish coursepublish = contentServiceClient.getCoursepublish(courseId);

        if (coursepublish == null)
        {
            return RestResponse.validfail("课程不存在");
        }

        // TODO: 查询是否支持试学


        // 用户已登录
        if (!StringUtils.isEmpty(userId))
        {
            XcCourseTablesDto xcCourseTablesDto = myCourseTablesService.getLearningStatus(userId, courseId);
            String learnStatus = xcCourseTablesDto.getLearnStatus();

            if ("702002".equals(learnStatus))
            {
                return RestResponse.validfail("无法学习，因为没有选课或选课后没有支付");
            }
            else if ("702003".equals(learnStatus))
            {
                return RestResponse.validfail("已过期需要重新续期或重新支付");
            }
            else
            {
                // 有资格学习 返回视频播放地址
                // 远程调用媒资获取视频播放地址
                RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
                return playUrlByMediaId;
            }
        }

        // 用户没有登录
        String charge = coursepublish.getCharge();

        // 免费课程
        if ("201000".equals(charge))
        {
            // 有资格学习 返回视频的播放地址
            // 远程调用媒资获取视频播放地址
            RestResponse<String> playUrlByMediaId = mediaServiceClient.getPlayUrlByMediaId(mediaId);
            return playUrlByMediaId;
        }

        return RestResponse.validfail("该课程没有选课");
    }
}
