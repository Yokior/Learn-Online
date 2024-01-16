package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/14 14:13
 */

public class MinioTest
{

    MinioClient minioClient = MinioClient.builder()
            .endpoint("http://127.0.0.1:9000")
            .credentials("minioadmin", "minioadmin")
            .build();


    @Test
    public void testUpload() throws Exception
    {

        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(".mp4");
        String minType = MediaType.APPLICATION_OCTET_STREAM_VALUE; // 通用minType 字节流
        if (extensionMatch != null)
        {
            minType = extensionMatch.getMimeType();
        }

        minioClient.uploadObject(
                UploadObjectArgs.builder()
                        .bucket("testbucket")
                        .object("BFV.mp4")
                        .filename("D:\\视频\\BFV剪辑版\\三连直接带走坦克.mp4")
                        .contentType(minType) // 设置媒体文件类型
                        .build()
        );
    }

    @Test
    public void testDelete() throws Exception
    {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket("testbucket")
                .object("BFV.mp4")
                .build());
    }


    @Test
    public void testDownload() throws Exception
    {
        // 直接下载到指定目录
//        minioClient.downloadObject(DownloadObjectArgs.builder()
//                .bucket("testbucket")
//                .object("BFV.mp4")
//                .filename("E:\\Minio\\la.mp4")
//                .build());

        GetObjectResponse object = minioClient.getObject(GetObjectArgs.builder()
                .bucket("testbucket")
                .object("BFV.mp4")
                .build());

        FileOutputStream fileOutputStream = new FileOutputStream(new File("E:\\Minio\\pp.mp4"));
        IOUtils.copy(object, fileOutputStream);

        // 校验文件完整性
//        String sourceMD5 = DigestUtils.md5Hex(object);
//        String localMD5 = DigestUtils.md5Hex(new FileInputStream(new File("E:\\Minio\\pp.mp4")));
//        if (sourceMD5.equals(localMD5))
//        {
//            System.out.println("下载成功");
//        }


    }

}
