package com.rent.faqweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/10/28

*/
@Controller
public class PageTestController {

    @RequestMapping("/test")
    public String testPage(){
        return "test";
    }

    @RequestMapping("/index")
    public String testIndex(){
        return "index";
    }
}
