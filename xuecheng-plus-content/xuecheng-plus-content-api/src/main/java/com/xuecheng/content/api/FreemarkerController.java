package com.xuecheng.content.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Description：
 * @Auther：Yokior
 * @Date：2024/1/28 14:34
 */
@Controller
public class FreemarkerController
{

    @GetMapping("/testfreemarker")
    public ModelAndView testFreemarker()
    {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("name1","啦啦啦");
        modelAndView.setViewName("test");

        return modelAndView;
    }

}
