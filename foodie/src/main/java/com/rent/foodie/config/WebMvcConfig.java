package com.rent.foodie.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/20

*/
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    //视图设置，无路径的请求默认跳转到index界面。
    public void addViewControllers(ViewControllerRegistry registry){
        registry.addViewController("/").setViewName("forward:/index.html");
        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);
        WebMvcConfigurer.super.addViewControllers(registry);
    }
}
