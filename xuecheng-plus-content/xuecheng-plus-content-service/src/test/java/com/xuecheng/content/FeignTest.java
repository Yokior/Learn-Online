package com.xuecheng.content;

import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/2 16:47
 */
@SpringBootTest
@Slf4j
public class FeignTest
{
    @Autowired
    private MediaServiceClient mediaServiceClient;


    @Test
    public void testFeign()
    {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(new File("C:\\Users\\Yokior\\AppData\\Local\\Temp\\course7174414956509230073.html"));
        if (multipartFile == null)
        {
            log.error("空");
            return;
        }
        String course = mediaServiceClient.upload(multipartFile, "course/" + "111" + ".html");
        log.error(course);

        if (course == null)
        {
            XueChengPlusException.cast("上传静态文件异常");
        }
    }


}
