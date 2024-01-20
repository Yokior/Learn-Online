package com.xuecheng.media;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Comparator;

/**
 * @Description：测试大文件上传
 * @Auther：Yokior
 * @Date：2024/1/18 14:42
 */
public class BigFileTest
{
    /**
     * 文件分块测试
     */
    @Test
    public void testChunk() throws IOException
    {
        // 源文件
        File sourceFile = new File("D:\\视频\\BFV剪辑版\\二式连杀11.mp4");
        // 分块文件存储路径
        String chunkFilePath = "D:\\视频\\分块测试即删\\";
        // 分块文件大小
        int chunkSize = 1024 * 1024 * 5; // 5MB
        // 分块文件个数
        int chunkNum = (int)Math.ceil(sourceFile.length() * 1.0  / chunkSize);
        // 使用流向源文件中读数据 向分块文件中写数据
        RandomAccessFile raf_r = new RandomAccessFile(sourceFile, "r");
        // 缓存区
        byte[] bytes = new byte[1024];
        // 每个分块单独分成一个文件
        for (int i = 0; i < chunkNum; i++)
        {
            File chunkFile = new File(chunkFilePath + i);
            RandomAccessFile raf_rw = new RandomAccessFile(chunkFile, "rw");
            int len = -1;
            // 没有读取结束就一直读取
            while ((len= raf_r.read(bytes)) != -1)
            {
                // 写入数据到分块
                raf_rw.write(bytes, 0, len);
                // 超过分块大小就跳出 下一个分块
                if (chunkFile.length() > chunkSize)
                {
                    break;
                }
            }
            raf_rw.close();
        }
        raf_r.close();

    }

    /**
     * 测试合并文件
     */
    @Test
    public void testMerge() throws IOException
    {
        // 块文件目录
        File chunkFilePath = new File("D:\\视频\\分块测试即删\\");
        // 源文件
        File sourceFile = new File("D:\\视频\\BFV剪辑版\\二式连杀11.mp4");
        // 合并后的文件
        File mergeFile = new File("D:\\视频\\分块测试即删\\合并.mp4");

        // 列出所有分块文件
        File[] files = chunkFilePath.listFiles();
        // 排序
        Arrays.sort(files, new Comparator<File>()
        {
            @Override
            public int compare(File o1, File o2)
            {
                return  Integer.parseInt(o1.getName()) - (Integer.parseInt(o2.getName()));
            }
        });

        // 缓冲区
        byte[] bytes = new byte[1024];
        // 合并后的文件写入流
        RandomAccessFile raf_rw = new RandomAccessFile(mergeFile, "rw");
        // 遍历分块文件 合并
        for (File file : files)
        {
            RandomAccessFile raf_r = new RandomAccessFile(file, "r");
            int len = -1;
            while ((len = raf_r.read(bytes)) != -1)
            {
                raf_rw.write(bytes, 0, len);
            }
            raf_r.close();
        }
        raf_rw.close();

        // 输出md5值 校验是否完整
        String sourceMD5 = DigestUtils.md5Hex(new FileInputStream(sourceFile));
        String mergeMD5 = DigestUtils.md5Hex(new FileInputStream(mergeFile));

        System.err.println("初始文件：" + sourceMD5);
        System.err.println("合并文件：" + mergeMD5);
    }




}
