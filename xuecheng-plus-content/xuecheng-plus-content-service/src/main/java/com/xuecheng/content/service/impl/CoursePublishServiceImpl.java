package com.xuecheng.content.service.impl;

import com.alibaba.fastjson.JSON;
import com.xuecheng.base.exception.CommonError;
import com.xuecheng.base.exception.XueChengPlusException;
import com.xuecheng.base.model.SystemCommon;
import com.xuecheng.content.config.MultipartSupportConfig;
import com.xuecheng.content.feignclient.MediaServiceClient;
import com.xuecheng.content.mapper.CourseBaseMapper;
import com.xuecheng.content.mapper.CourseMarketMapper;
import com.xuecheng.content.mapper.CoursePublishMapper;
import com.xuecheng.content.mapper.CoursePublishPreMapper;
import com.xuecheng.content.model.dto.CourseBaseInfoDto;
import com.xuecheng.content.model.dto.CoursePreviewDto;
import com.xuecheng.content.model.dto.TeachplanDto;
import com.xuecheng.content.model.entity.CourseBase;
import com.xuecheng.content.model.entity.CourseMarket;
import com.xuecheng.content.model.entity.CoursePublish;
import com.xuecheng.content.model.entity.CoursePublishPre;
import com.xuecheng.content.service.CourseBaseInfoService;
import com.xuecheng.content.service.CoursePublishService;
import com.xuecheng.content.service.TeachplanService;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/29 15:16
 */
@Slf4j
@Service
public class CoursePublishServiceImpl implements CoursePublishService
{

    @Autowired
    private CourseBaseInfoService courseBaseInfoService;

    @Autowired
    private TeachplanService teachplanService;

    @Autowired
    private CourseBaseMapper courseBaseMapper;

    @Autowired
    private CourseMarketMapper courseMarketMapper;

    @Autowired
    private CoursePublishMapper coursePublishMapper;

    @Autowired
    private CoursePublishPreMapper coursePublishPreMapper;

    @Autowired
    private MqMessageService mqMessageService;

    @Autowired
    private MediaServiceClient mediaServiceClient;

    @Override
    public CoursePreviewDto getCoursePreviewInfo(Long courseId)
    {
        CoursePreviewDto coursePreviewDto = new CoursePreviewDto();

        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        coursePreviewDto.setCourseBase(courseBaseInfo);

        List<TeachplanDto> treeNodes = teachplanService.getTreeNodes(courseId);
        coursePreviewDto.setTeachplans(treeNodes);

        return coursePreviewDto;
    }

    /**
     * 提交审核
     *
     * @param companyId
     * @param courseId
     */
    @Transactional
    @Override
    public void commitAudit(Long companyId, Long courseId)
    {
        // 查询状态：已提交则不允许提交
        CourseBaseInfoDto courseBaseInfo = courseBaseInfoService.getCourseBaseInfo(courseId);
        if (courseBaseInfo == null)
        {
            XueChengPlusException.cast("课程信息不存在");
        }
        String auditStatus = courseBaseInfo.getAuditStatus();
        if (SystemCommon.AUDIT_IS_PUBLISH.equals(auditStatus))
        {
            XueChengPlusException.cast("课程已提交，等待审核结果");
        }

        // 课程图片 计划信息没有填写不允许提交
        String pic = courseBaseInfo.getPic();
        if (StringUtils.isEmpty(pic))
        {
            XueChengPlusException.cast("课程图片不能为空");
        }
        List<TeachplanDto> teachPlanTree = teachplanService.getTreeNodes(courseId);
        if (teachPlanTree.isEmpty())
        {
            XueChengPlusException.cast("课程计划不能为空");
        }

        CoursePublishPre coursePublishPre = new CoursePublishPre();

        // 查询课程基本信息
        BeanUtils.copyProperties(courseBaseInfo, coursePublishPre);

        // 查询营销信息
        CourseMarket courseMarket = courseMarketMapper.selectById(courseId);
        if (courseMarket == null)
        {
            XueChengPlusException.cast("营销信息不存在");
        }
        String courseMarketJson = JSON.toJSONString(courseMarket);
        coursePublishPre.setMarket(courseMarketJson);

        // 查询计划信息
        String teachPlanJson = JSON.toJSONString(teachPlanTree);
        coursePublishPre.setTeachplan(teachPlanJson);

        // 设置机构的ID
        coursePublishPre.setCompanyId(companyId);

        // TODO: 本机构只能发布自己的视频

        // 查询预发布表 如果有就更新 没有就插入
        CoursePublishPre dbCoursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (dbCoursePublishPre == null)
        {
            coursePublishPreMapper.insert(coursePublishPre);
        }
        else
        {
            coursePublishPreMapper.updateById(coursePublishPre);
        }

        // 更新课程基本信息为已提交
        CourseBase courseBase = courseBaseMapper.selectById(courseId);
        courseBase.setAuditStatus(SystemCommon.AUDIT_IS_PUBLISH);
        courseBaseMapper.updateById(courseBase);
    }

    /**
     * 课程发布
     *
     * @param companyId
     * @param courseId
     */
    @Transactional
    @Override
    public void publish(Long companyId, Long courseId)
    {
        // 查询预发布表
        CoursePublishPre coursePublishPre = coursePublishPreMapper.selectById(courseId);
        if (coursePublishPre == null)
        {
            XueChengPlusException.cast("课程没有审核记录，无法发布");
        }

        String status = coursePublishPre.getStatus();

        if (!SystemCommon.AUDIT_PASS.equals(status))
        {
            XueChengPlusException.cast("课程审核未通过，禁止发布");
        }

        // 向发布表写入数据
        CoursePublish coursePublish = new CoursePublish();
        BeanUtils.copyProperties(coursePublishPre, coursePublish);
        CoursePublish dbCoursePublish = coursePublishMapper.selectById(courseId);

        // 查询记录 有则更新 无则添加
        if (dbCoursePublish == null)
        {
            coursePublishMapper.insert(coursePublish);
        }
        else
        {
            coursePublishMapper.updateById(coursePublish);
        }

        // 向消息表写入数据
        saveCoursePublishMessage(courseId);

        // 将预发布表删除
        coursePublishPreMapper.deleteById(courseId);


    }

    @Override
    public File generateCourseHtml(Long courseId)
    {

        //静态化文件
        File htmlFile = null;

        try
        {
            //配置freemarker
            Configuration configuration = new Configuration(Configuration.getVersion());

            //加载模板
            //选指定模板路径,classpath下templates下
            //得到classpath路径
            String classpath = this.getClass().getResource("/").getPath();
            configuration.setDirectoryForTemplateLoading(new File(classpath + "/templates/"));
            //设置字符编码
            configuration.setDefaultEncoding("utf-8");

            //指定模板文件名称
            Template template = configuration.getTemplate("course_template.ftl");

            //准备数据
            CoursePreviewDto coursePreviewInfo = this.getCoursePreviewInfo(courseId);

            Map<String, Object> map = new HashMap<>();
            map.put("model", coursePreviewInfo);

            //静态化
            //参数1：模板，参数2：数据模型
            String content = FreeMarkerTemplateUtils.processTemplateIntoString(template, map);
//            System.out.println(content);
            //将静态化内容输出到文件中
            InputStream inputStream = IOUtils.toInputStream(content);
            //创建静态化文件
            htmlFile = File.createTempFile("course", ".html");
            log.debug("课程静态化，生成静态文件:{}", htmlFile.getAbsolutePath());
            //输出流
            FileOutputStream outputStream = new FileOutputStream(htmlFile);
            IOUtils.copy(inputStream, outputStream);
        }
        catch (Exception e)
        {
            log.error("课程静态化异常:{}", e.toString());
            XueChengPlusException.cast("课程静态化异常");
        }

        return htmlFile;
    }

    @Override
    public void uploadCourseHtml(Long courseId, File file)
    {
        MultipartFile multipartFile = MultipartSupportConfig.getMultipartFile(file);
        String course = mediaServiceClient.upload(multipartFile, "course/" + courseId + ".html");
        if (course == null)
        {
            XueChengPlusException.cast("上传静态文件异常");
        }
    }


    private void saveCoursePublishMessage(Long courseId)
    {
        MqMessage mqMessage = mqMessageService.addMessage("course_publish", String.valueOf(courseId), null, null);
        if (mqMessage == null)
        {
            XueChengPlusException.cast(CommonError.UNKOWN_ERROR);
        }
    }
}
