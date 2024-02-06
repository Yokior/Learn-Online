package com.xuecheng.content.util;

import com.alibaba.fastjson.JSON;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/2/6 16:12
 */
@Slf4j
public class SecurityUtil
{
    public static XcUser getUser()
    {
        try
        {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof String)
            {
                String xcUserStr = principal.toString();
                // 将JSON转换为对象
                XcUser xcUser = JSON.parseObject(xcUserStr, XcUser.class);
                return xcUser;
            }
        }
        catch (Exception e)
        {
            log.error("获取用户信息失败,{}", e.getMessage());
            e.printStackTrace();
        }
        return null;
    }



    @Data
    public static class XcUser implements Serializable
    {

        private static final long serialVersionUID = 1L;

        private String id;

        private String username;

        private String password;

        private String salt;

        private String name;
        private String nickname;
        private String wxUnionid;
        private String companyId;
        /**
         * 头像
         */
        private String userpic;

        private String utype;

        private LocalDateTime birthday;

        private String sex;

        private String email;

        private String cellphone;

        private String qq;

        /**
         * 用户状态
         */
        private String status;

        private LocalDateTime createTime;

        private LocalDateTime updateTime;


    }
}
