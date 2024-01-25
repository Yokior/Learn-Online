package com.xuecheng.media.service.impl;

import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    /**
     * 根据分片索引查询需要分配的任务
     *
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
     *
     * @param id
     * @return
     */
    @Override
    public boolean startTask(long id)
    {
        int result = mediaProcessMapper.startTask(id);
        return result > 0;
    }

    /**
     * 保存任务处理结果
     *
     * @param taskId
     * @param status
     * @param fileId
     * @param url
     * @param errorMsg
     */
    @Transactional
    @Override
    public void saveProcessFinishStatus(Long taskId, String status, String fileId, String url, String errorMsg)
    {
        MediaProcess mediaProcess = mediaProcessMapper.selectById(taskId);
        if (mediaProcess == null)
        {
            return;
        }

        // 任务执行失败
        if ("3".equals(status))
        {
            mediaProcess.setStatus("3");
            mediaProcess.setFailCount(mediaProcess.getFailCount() + 1);
            mediaProcess.setErrormsg(errorMsg);
            mediaProcessMapper.updateById(mediaProcess);

            return;
        }

        // 任务执行成功

        // 更新media_file表中的数据
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileId);
        mediaFiles.setUrl(url);
        mediaFilesMapper.updateById(mediaFiles);

        // 更新media_process表中的数据
        mediaProcess.setStatus("2");
        mediaProcess.setFinishDate(LocalDateTime.now());
        mediaProcess.setUrl(url);
        mediaProcessMapper.updateById(mediaProcess);

        // 向media_process_history表中插入数据
        MediaProcessHistory mediaProcessHistory = new MediaProcessHistory();
        BeanUtils.copyProperties(mediaProcess, mediaProcessHistory);
        // 设置id为null 防止主键冲突
        mediaProcessHistory.setId(null);
        mediaProcessHistoryMapper.insert(mediaProcessHistory);

        // 删除media_process记录
        mediaProcessMapper.deleteById(taskId);

    }
}
