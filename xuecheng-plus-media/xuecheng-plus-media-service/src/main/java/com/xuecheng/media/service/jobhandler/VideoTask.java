package com.xuecheng.media.service.jobhandler;

import com.xuecheng.base.utils.Mp4VideoUtil;
import com.xuecheng.media.mapper.MediaProcessHistoryMapper;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.model.po.MediaProcessHistory;
import com.xuecheng.media.service.MediaFileProcessService;
import com.xuecheng.media.service.MediaFileService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class VideoTask
{

    @Autowired
    private MediaFileProcessService mediaFileProcessService;

    @Autowired
    private MediaFileService mediaFileService;

    @Autowired
    private MediaProcessHistoryMapper mediaProcessHistoryMapper;

    @Value("${videoprocess.ffmpegpath}")
    private String ffmpegpath;

    /**
     * 视频处理任务
     */
    @XxlJob("videoJobHandler")
    public void videoJobHandler()
    {

        // 分片参数
        int shardIndex = XxlJobHelper.getShardIndex(); // 执行器序号 0开始
        int shardTotal = XxlJobHelper.getShardTotal(); // 执行器总数

        int processors = Runtime.getRuntime().availableProcessors();

        // 查询待处理的任务
        List<MediaProcess> mediaProcessList = mediaFileProcessService.getMediaProcessList(shardTotal, shardIndex, processors);

        int size = mediaProcessList.size();

        ExecutorService executorService = Executors.newFixedThreadPool(size);

        if (mediaProcessList.isEmpty())
        {
            return;
        }

        CountDownLatch count = new CountDownLatch(size);

        // 创建线程池
        mediaProcessList.forEach(mediaProcess ->
        {
            // 将任务加入线程池
            executorService.execute(() ->
            {
                try
                {
                    Long taskId = mediaProcess.getId();
                    // 开启任务
                    boolean b = mediaFileProcessService.startTask(taskId);
                    if (!b)
                    {
                        log.info("抢占任务失败，任务id:{}", taskId);
                        return;
                    }

                    String bucket = mediaProcess.getBucket();
                    String objectName = mediaProcess.getFilePath();
                    String fileId = mediaProcess.getFileId();

                    // 执行视频转码

                    File file = mediaFileService.downloadFileFromMinio(bucket, objectName);
                    if (file == null)
                    {
                        log.info("下载视频失败，任务id:{}, bucket:{}, objectName:{}", taskId, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "下载视频到本地失败");
                        return;
                    }


                    //源avi视频的路径
                    String video_path = file.getAbsolutePath();
                    //转换后mp4文件的名称
                    String mp4_name = fileId + ".mp4";
                    //转换后mp4文件的路径
                    // 创建临时文件保存位置
                    File mp4File = null;
                    try
                    {
                        mp4File = File.createTempFile("minio", ".mp4");
                    }
                    catch (IOException e)
                    {
                        log.info("创建临时文件异常,{}", e.getMessage());
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "创建临时文件失败");
                        e.printStackTrace();
                        return;
                    }
                    finally
                    {
                        if (mp4File != null)
                        {
                            mp4File.deleteOnExit();
                        }
                    }

                    String mp4_path = mp4File.getAbsolutePath();
                    //创建工具类对象
                    Mp4VideoUtil videoUtil = new Mp4VideoUtil(ffmpegpath, video_path, mp4_name, mp4_path);
                    //开始视频转换，成功将返回success
                    String s = videoUtil.generateMp4();
                    if (!s.equals("success"))
                    {
                        log.info("视频转码失败,原因:{},bucket:{},objectName:{}", s, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "视频转码失败");
                        return;
                    }
                    // 上传到minio
                    String newObjectName = getFilePath(fileId, ".mp4");
                    Boolean uploadResult = mediaFileService.uploadFile2Minio(bucket, mp4File.getAbsolutePath(), "video/mp4", newObjectName);
                    if (!uploadResult)
                    {
                        log.info("上传mp4到minio失败,taskId:{},bucket:{},objectName:{}", taskId, bucket, objectName);
                        mediaFileProcessService.saveProcessFinishStatus(taskId, "3", fileId, null, "上传文件到minio失败");
                        return;
                    }

                    // mp4文件的url
                    String url = "/" + bucket + "/" + newObjectName;

                    // 处理成功 保存任务处理结果
                    mediaFileProcessService.saveProcessFinishStatus(taskId, "2", fileId, url, null);
                }
                catch (Exception e)
                {
                    log.info("出现异常:{}", e.getMessage());
                    return;
                }
                finally
                {
                    // 计数器减1
                    count.countDown();
                }
            });
        });

        // 阻塞
        try
        {
            // 指定最大限度阻塞时间 30分钟
            count.await(30, TimeUnit.MINUTES);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }


    }


    /**
     * 根据md5值获取最终文件路径
     *
     * @param md5
     * @param ext
     * @return
     */
    private String getFilePath(String md5, String ext)
    {
        return md5.charAt(0) + "/" + md5.charAt(1) + "/" + md5 + "/" + md5 + ext;
    }


}
