package com.xuecheng.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.PageParams;
import com.xuecheng.base.model.PageResult;
import com.xuecheng.media.mapper.MediaFilesMapper;
import com.xuecheng.media.model.dto.QueryMediaParamsDto;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.model.dto.UploadFileResultDto;
import com.xuecheng.media.model.po.MediaFiles;
import com.xuecheng.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2022/9/10 8:58
 */
@Service
public class MediaFileServiceImpl implements MediaFileService
{

    @Autowired
    MediaFilesMapper mediaFilesMapper;

    @Autowired
    private MinioClient minioClient;

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

        uploadFile2Minio(bucket_mediafiles,localFilePath,minType,objectName);

        // 将文件信息保存到数据库


        return null;
    }


    /**
     * 获取文件的md5
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
     * @return
     */
    private String getDefaultFolderPath()
    {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yy-MM-dd");
        String folder = simpleDateFormat.format(new Date()).replace("-", "/") + "/";
        return folder;
    }


    /**
     * 上传文件到Minio
     * @param bucket
     * @param localFilePath
     * @param minType
     * @param objectName
     * @return
     */
    private Boolean uploadFile2Minio(String bucket, String localFilePath, String minType, String objectName)
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
