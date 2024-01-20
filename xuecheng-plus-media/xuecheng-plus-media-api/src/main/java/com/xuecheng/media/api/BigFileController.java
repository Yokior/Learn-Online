package com.xuecheng.media.api;

import com.xuecheng.base.model.RestResponse;
import com.xuecheng.base.model.SystemCommon;
import com.xuecheng.media.model.dto.UploadFileParamsDto;
import com.xuecheng.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/19 14:42
 */
@Api(value = "大文件上传接口", tags = "大文件上传接口")
@RestController
public class BigFileController
{

    @Autowired
    private MediaFileService mediaFileService;


    @ApiOperation(value = "文件上传前检查文件")
    @PostMapping("/upload/checkfile")
    public RestResponse<Boolean> checkfile(@RequestParam("fileMd5") String fileMd5) throws Exception
    {
        RestResponse<Boolean> response = mediaFileService.checkFile(fileMd5);
        return response;
    }

    @ApiOperation(value = "分块文件上传前的检测")
    @PostMapping("/upload/checkchunk")
    public RestResponse<Boolean> checkchunk(@RequestParam("fileMd5") String fileMd5, @RequestParam("chunk") int chunk) throws Exception
    {
        RestResponse<Boolean> response = mediaFileService.checkChunk(fileMd5, chunk);
        return response;
    }

    @ApiOperation(value = "上传分块文件")
    @PostMapping("/upload/uploadchunk")
    public RestResponse uploadchunk(@RequestParam("file") MultipartFile file,
                                    @RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("chunk") int chunk) throws Exception
    {
        // 创建临时文件
        File tempFile = File.createTempFile("minio", ".temp");
        file.transferTo(tempFile);
        // 获取本机文件路径
        String localFilePath = tempFile.getAbsolutePath();

        RestResponse response = mediaFileService.uploadChunk(fileMd5, chunk, localFilePath);

        return response;
    }

    @ApiOperation(value = "合并文件")
    @PostMapping("/upload/mergechunks")
    public RestResponse mergechunks(@RequestParam("fileMd5") String fileMd5,
                                    @RequestParam("fileName") String fileName,
                                    @RequestParam("chunkTotal") int chunkTotal) throws Exception
    {
        Long companyId = 1234567890L;

        // 文件信息
        UploadFileParamsDto uploadFileParamsDto = new UploadFileParamsDto();
        uploadFileParamsDto.setFilename(fileName);
        uploadFileParamsDto.setTags("视频文件");
        uploadFileParamsDto.setFileType(SystemCommon.RESOURCE_TYPE_VIDEO);

        RestResponse response = mediaFileService.mergeChunks(companyId, fileMd5, chunkTotal, uploadFileParamsDto);

        return response;
    }


}

