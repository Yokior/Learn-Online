package com.xuecheng.media;

import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import io.minio.*;
import io.minio.errors.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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


    /**
     * 将分块文件上传到minio
     */
    @Test
    public void uploadChunk() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException
    {
        File chunkFilePath = new File("D:\\视频\\分块测试即删\\");
        int chunkFileCount = chunkFilePath.listFiles().length;

        for (int i = 0; i < chunkFileCount; i++)
        {
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket("testbucket")
                            .object("chunk/" + i)
                            .filename("D:\\视频\\分块测试即删\\" + i)
                            .build()
            );
        }
    }

    /**
     * 合并分块文件
     */
    @Test
    public void testMerge() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException
    {
        File chunkFilePath = new File("D:\\视频\\分块测试即删\\");
        int chunkFileCount = chunkFilePath.listFiles().length;

//        List<ComposeSource> composeSources = new ArrayList<>();
//
//        for (int i = 0; i < chunkFileCount; i++)
//        {
//            ComposeSource chunk = ComposeSource.builder()
//                    .bucket("testbucket")
//                    .object("chunk/" + i)
//                    .build();
//            composeSources.add(chunk);
//        }

        List<ComposeSource> composeSourceList = Stream.iterate(0, i -> i + 1).limit(chunkFileCount)
                .map(i -> ComposeSource.builder()
                        .bucket("testbucket")
                        .object("chunk/" + i)
                        .build())
                .collect(Collectors.toList());


        ComposeObjectArgs composeObjectArgs = ComposeObjectArgs.builder()
                .bucket("testbucket")
                .object("merge.mp4")
                .sources(composeSourceList)
                .build();
        // 合并文件
        minioClient.composeObject(composeObjectArgs);
    }

}
