package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/23 15:36
 */
@Service
public class MediaFileProcessServiceImpl implements MediaFileProcessService
{

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    /**
     * 根据分片索引查询需要分配的任务
     * @param shardIndex
     * @param shardTotal
     * @param count
     * @return
     */
    @Override
    public List<MediaProcess> getMediaProcessList(int shardIndex, int shardTotal, int count)
    {
        // 获取待处理任务列表
        List<MediaProcess> mediaProcessList = mediaProcessMapper.selectListByShardIndex(shardIndex, shardTotal, count);




        return null;
    }

    /**
     * 开启任务
     * @param id
     * @return
     */
    @Override
    public boolean startTask(long id)
    {
        int result = mediaProcessMapper.startTask(id);
        return result > 0;
    }
}
