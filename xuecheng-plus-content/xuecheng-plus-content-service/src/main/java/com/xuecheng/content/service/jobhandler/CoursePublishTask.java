package com.xuecheng.content.service.jobhandler;

import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MessageProcessAbstract;
import com.xuecheng.messagesdk.service.MqMessageService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/31 14:47
 */
@Component
@Slf4j
public class CoursePublishTask extends MessageProcessAbstract
{

    @XxlJob("CoursePublishJobHandler")
    public void coursePublishJobHandler()
    {
        int shardIndex = XxlJobHelper.getShardIndex();
        int shardTotal = XxlJobHelper.getShardTotal();

        process(shardIndex, shardTotal, "course_publish",30,60);
    }



    /**
     * 执行发布任务逻辑
     *
     * @param mqMessage 执行任务内容
     * @return
     */
    @Override
    public boolean execute(MqMessage mqMessage)
    {
        Long courseId = Long.valueOf(mqMessage.getBusinessKey1());
        Long id = mqMessage.getId();

        MqMessageService mqMessageService = getMqMessageService();
        int stageOne = mqMessageService.getStageOne(id);
        int stageTwo = mqMessageService.getStageTwo(id);
        int stageThree = mqMessageService.getStageThree(id);

        // 课程静态化上传minio
        if (stageOne > 0)
        {
            log.info("课程静态化任务完成，无需处理");
        }
        else
        {
            generateCourseHtml(mqMessage, courseId);
        }

        // 向es写索引数据
        if (stageTwo > 0)
        {
            log.info("课程索引任务完成，无需处理");
        }
        else
        {
            saveCourseIndex(mqMessage, courseId);
        }

        // 向redis写缓存
        if (stageThree > 0)
        {
            log.info("课程缓存任务完成，无需处理");
        }
        else
        {
            saveCourseCache(mqMessage, courseId);
        }


        // 返回true表示任务完成

        return true;
    }

    /**
     * 课程静态化上传minio
     *
     * @param mqMessage
     * @param courseId
     */
    private void generateCourseHtml(MqMessage mqMessage, Long courseId)
    {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();

        // 进行课程静态化




        mqMessageService.completedStageOne(taskId);
    }


    /**
     * 向es写索引数据
     *
     * @param mqMessage
     * @param courseId
     */
    public void saveCourseIndex(MqMessage mqMessage, Long courseId)
    {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();

        // 写入索引





        mqMessageService.completedStageTwo(taskId);
    }


    /**
     *  向redis写缓存
     * @param mqMessage
     * @param courseId
     */
    public void saveCourseCache(MqMessage mqMessage, Long courseId)
    {
        Long taskId = mqMessage.getId();
        MqMessageService mqMessageService = getMqMessageService();

        // 写入缓存


        mqMessageService.completedStageThree(taskId);
    }

}
