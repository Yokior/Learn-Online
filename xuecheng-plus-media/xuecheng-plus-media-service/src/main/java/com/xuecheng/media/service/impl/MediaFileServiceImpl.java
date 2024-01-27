package com.xuecheng.media.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.model.SystemCommon;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.mapper.MediaProcessMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.model.po.MediaProcess;
import com.xuecheng.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
@Slf4j
public class MediaFileServiceImpl implements MediaFileService
{

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MediaProcessMapper mediaProcessMapper;

    @Autowired
    private MinioClient minioClient;

    @Autowired
    private MediaFileService mediaProxy;

    @Value("${minio.bucket.files}")
    private String bucket_mediafiles;

    @Value("${minio.bucket.videofiles}")
    private String bucket_video;


    @Override
    public PageResult<MediaFiles> queryMediaFiels(Long companyId, PageParams pageParams, QueryMediaParamsDto queryMediaParamsDto)
    {

        //构建查询条件对象
        LambdaQueryWrapper<MediaFiles> queryWrapper = new LambdaQueryWrapper<>();

        //分页对象
        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        // 查询数据内容获得结果
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, queryWrapper);
        // 获取数据列表
        List<MediaFiles> list = pageResult.getRecords();
        // 获取数据总数
        long total = pageResult.getTotal();
        // 构建结果集
        PageResult<MediaFiles> mediaListResult = new PageResult<>(list, total, pageParams.getPageNo(), pageParams.getPageSize());
        return mediaListResult;

    }

    /**
     * 上传文件
     *
     * @param companyId
     * @param uploadFileParamsDto
     * @param localFilePath
     * @return
     */
    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto uploadFileParamsDto, String localFilePath)
    {
        String filename = uploadFileParamsDto.getFilename();
        String extension = filename.substring(filename.lastIndexOf("."));

        // 获取minType
        String minType = getMinType(extension);
        // 获取目录路径
        String defaultFolderPath = getDefaultFolderPath();
        // 获取文件的md5值用于命名
        String md5 = getFileMD5(new File(localFilePath));
        // 生成文件名
        String objectName = defaultFolderPath + md5 + extension;

        Boolean result = uploadFile2Minio(bucket_mediafiles, localFilePath, minType, objectName);
        if (!result)
        {
            XueChengPlusException.cast("文件上传失败");
        }

        // 将文件信息保存到数据库
        MediaFiles mediaFiles = mediaProxy.saveFileInfo2DB(companyId, uploadFileParamsDto, md5, bucket_mediafiles, objectName);
        if (mediaFiles == null)
        {
            XueChengPlusException.cast("文件上传后保存信息失败");
        }

        UploadFileResultDto uploadFileResultDto = new UploadFileResultDto();
        BeanUtils.copyProperties(mediaFiles, uploadFileResultDto);

        return uploadFileResultDto;
    }

    /**
     * 将文件信息保存在数据库
     *
     * @param companyId
     * @param uploadFileParamsDto
     * @param md5
     */
    @Transactional
    @Override
    public MediaFiles saveFileInfo2DB(Long companyId, UploadFileParamsDto uploadFileParamsDto, String md5, String bucket, String objectName)
    {
        MediaFiles dbMediaFiles = mediaFilesMapper.selectById(md5);
        MediaFiles mediaFiles = new MediaFiles();
        if (dbMediaFiles == null)
        {
            // 数据库没有信息 保存信息
            BeanUtils.copyProperties(uploadFileParamsDto, mediaFiles);
            mediaFiles.setId(md5);
            mediaFiles.setCompanyId(companyId);
            mediaFiles.setBucket(bucket);
            mediaFiles.setFilePath(objectName);
            mediaFiles.setFileId(md5);
            mediaFiles.setUrl("/" + bucket + "/" + objectName);
            // 状态
            mediaFiles.setStatus("1");
            // 审核状态
            mediaFiles.setAuditStatus(SystemCommon.OBJ_AUDIT_PASS);

            int insert = mediaFilesMapper.insert(mediaFiles);
            if (insert <= 0)
            {
                log.error("保存文件信息失败,文件信息:{}", JSON.toJSONString(mediaFiles));
                return null;
            }
        }

        // 判断如果是avi视频写入待处理任务（视频转码）
        saveWaitingTask(mediaFiles);

        return mediaFiles;
    }

    /**
     * 从minio下载文件
     * @param bucket
     * @param objectName
     * @return
     */
    @Override
    public File downloadFileFromMinio(String bucket, String objectName)
    {
        File minioFile = null;
        FileOutputStream outputStream = null;

        try
        {
            GetObjectResponse input = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .build());
            // 创建临时文件
            minioFile = File.createTempFile("minio", ".merge");
            outputStream = new FileOutputStream(minioFile);
            IOUtils.copy(input,outputStream);
            return minioFile;
        }
        catch (Exception e)
        {
            return null;
        }
        finally
        {
            try
            {
                if (outputStream != null)
                {
                    outputStream.close();
                }
            }
            catch (IOException e)
            {
                log.error("关闭流失败");
            }
        }
    }

    /**
     * 添加待处理任务
     * @param mediaFiles
     */
    private void saveWaitingTask(MediaFiles mediaFiles)
    {
        // 获取文件名称
        String filename = mediaFiles.getFilename();
        // 文件扩展名
        String extension = filename.substring(filename.lastIndexOf("."));
        String minType = getMinType(extension);
        // 如果是avi视频 写入待处理任务
        if ("video/x-msvideo".equals(minType))
        {
            MediaProcess mediaProcess = new MediaProcess();
            BeanUtils.copyProperties(mediaFiles,mediaProcess);
            // 状态： 未处理
            mediaProcess.setStatus("1");
            mediaProcess.setUrl(null);
            mediaProcessMapper.insert(mediaProcess);
        }
    }


    /**
     * 检查文件是否存在
     *
     * @param fileMd5 文件的md5
     * @return
     */
    @Override
    public RestResponse<Boolean> checkFile(String fileMd5)
    {
        // 先查询数据库
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null)
        {
            String bucket = mediaFiles.getBucket();
            String filePath = mediaFiles.getFilePath();
            // 再查询minio
            try
            {
                GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(filePath)
                        .build());
                if (object != null)
                {
                    return RestResponse.success(true);
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }

        // 文件不存在

        return RestResponse.success(false);
    }

    /**
     * 查询分块是否存在
     *
     * @param fileMd5    文件的md5
     * @param chunkIndex 分块序号
     * @return
     */
    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex)
    {
        // 获取路径
        String chunkFilePath = getChunkFilePath(fileMd5);
        chunkFilePath = chunkFilePath + chunkIndex;

        // 查询minio
        try
        {
            GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket_video)
                    .object(chunkFilePath)
                    .build());
            if (object != null)
            {
                return RestResponse.success(true);
            }
        }
        catch (Exception e)
        {
            // 文件不存在
            return RestResponse.success(false);
        }

        // 文件不存在
        return RestResponse.success(false);
    }

    /**
     * 上传分块文件到minio
     *
     * @param fileMd5
     * @param chunk
     * @param localChunkFilePath
     * @return
     */
    @Override
    public RestResponse uploadChunk(String fileMd5, int chunk, String localChunkFilePath)
    {
        String minType = getMinType(null);
        String chunkFilePath = getChunkFilePath(fileMd5);
        chunkFilePath += chunk;

        Boolean upResult = uploadFile2Minio(bucket_video, localChunkFilePath, minType, chunkFilePath);
        if (!upResult)
        {
            // 上传分块失败
            return RestResponse.validfail(false, "上传分块文件失败");
        }

        // 上传分块成功
        return RestResponse.success(true);
    }

    /**
     * 合并分块文件
     *
     * @param companyId
     * @param fileMd5
     * @param chunkTotal
     * @param uploadFileParamsDto
     * @return
     */
    @Transactional
    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto)
    {
        String chunkFilePath = getChunkFilePath(fileMd5);
        String filename = uploadFileParamsDto.getFilename();
        String filePath = getFilePath(fileMd5, filename.substring(filename.lastIndexOf(".")));
//        // 合并后的md5值
//        String etag = "";
//        String etag_st = "";

        // 找到分块文件调用minio的sdk进行文件合并
        List<ComposeSource> composeSourceList = Stream.iterate(0, i -> i + 1).limit(chunkTotal)
                .map(i -> ComposeSource.builder()
                        .bucket(bucket_video)
                        .object(chunkFilePath + i)
                        .build())
                .collect(Collectors.toList());


        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(filePath)
                .sources(composeSourceList)
                .build();
        // 合并文件
        try
        {
            ObjectWriteResponse response = minioClient.composeObject(composeObjectArgs);
            // 获取文件的信息
            StatObjectResponse statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket_video)
                    .object(filePath)
                    .build());
            // 设置文件的大小
            uploadFileParamsDto.setFileSize(statObjectResponse.size());
//            etag = response.etag();
//            etag_st = statObjectResponse.etag();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error("合并文件出错,bucket:{},objectName:{},错误:{}",bucket_video,filePath,e.getMessage());
            return RestResponse.validfail(false,"合并文件出错");
        }

//        log.info("源文件：{}",fileMd5);
//        log.info("ETG：{}",etag);
//        log.info("ETG_st：{}",etag_st);

        // 校验合并后的和源文件是否一致
//        if (!fileMd5.equals(etag))
//        {
//            return RestResponse.validfail(false,"合并后的文件和源文件不一致");
//        }

        // 将文件信息入库
        MediaFiles mediaFiles = mediaProxy.saveFileInfo2DB(companyId, uploadFileParamsDto, fileMd5, bucket_video, filePath);
        if (mediaFiles == null)
        {
            return RestResponse.validfail(false,"文件信息入库失败");
        }

        // 清理分块文件
        clearChunkFiles(chunkFilePath,chunkTotal);

        return RestResponse.success(true);
    }


    /**
     * 清理分块文件
     * @param chunkFilePath
     * @param chunkTotal
     */
    private void clearChunkFiles(String chunkFilePath, int chunkTotal)
    {
        Iterable<DeleteObject> deleteObjects = Stream.iterate(0, i -> i+1)
                .limit(chunkTotal)
                .map(i -> new DeleteObject(chunkFilePath + i))
                .collect(Collectors.toList());

        RemoveObjectsArgs removeObjectsArgs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(deleteObjects).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjectsArgs);

        results.forEach(r -> {
            try
            {
                DeleteError deleteError = r.get();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }



    /**
     * 根据md5获取分块文件路径
     * @param md5
     * @return
     */
    private String getChunkFilePath(String md5)
    {
        return md5.charAt(0) + "/" + md5.charAt(1) + "/" + md5 + "/" + "chunk" + "/";
    }

    /**
     * 根据md5值获取最终文件路径
     * @param md5
     * @param ext
     * @return
     */
    private String getFilePath(String md5, String ext)
    {
        return md5.charAt(0) + "/" + md5.charAt(1) + "/" + md5 + "/" + md5 + ext;
    }


    /**
     * 获取文件的md5
     *
     * @return
     */
    private String getFileMD5(File file)
    {
        String md5 = "";

        // 获取md5
        try
        {
            FileInputStream fileInputStream = new FileInputStream(file);
            md5 = DigestUtils.md5DigestAsHex(fileInputStream);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return md5;
    }

    /**
     * 根据当前日期获取目录路径
     * 2024/1/16
     *
     * @return
     */
    private String getDefaultFolderPath()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String folder = simpleDateFormat.format(new Date()).replace("-", "/") + "/";
        return folder;
    }


    /**
     * 上传文件到Minio
     *
     * @param bucket
     * @param localFilePath
     * @param minType
     * @param objectName
     * @return
     */
    public Boolean uploadFile2Minio(String bucket, String localFilePath, String minType, String objectName)
    {
        UploadObjectArgs uploadObjectArgs = null;
        try
        {
            uploadObjectArgs = UploadObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectName)
                    .filename(localFilePath)
                    .contentType(minType) // 设置媒体文件类型
                    .build();

            minioClient.uploadObject(uploadObjectArgs);

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            XueChengPlusException.cast(CommonError.PARAMS_ERROR);
        }
        return false;
    }

    /**
     * 根据后缀生成minType
     *
     * @param extension
     * @return
     */
    private String getMinType(String extension)
    {
        if (extension == null)
        {
            extension = "";
        }

        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String minType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 通用minType 字节流
        if (extensionMatch != null)
        {
            minType = extensionMatch.getMimeType();
        }
        return minType;
    }
}
