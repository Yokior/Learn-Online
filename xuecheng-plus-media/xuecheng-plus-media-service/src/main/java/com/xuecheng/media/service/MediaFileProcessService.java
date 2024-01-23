package com.xuecheng.media.service;

import com.xuecheng.media.model.po.MediaProcess;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/23 15:36
 */
public interface MediaFileProcessService
{
    /**
     * 根据分片索引查询需要分配的任务
     * @param shardIndex
     * @param shardTotal
     * @param count
     * @return
     */
    List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count);


    /**
     * 开启一个任务
     * @param id
     * @return
     */
    boolean startTask(long id);

}
