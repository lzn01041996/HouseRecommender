package com.rent.foodie.controller;

import com.rent.foodie.service.House2SearchService;
import com.rent.foodie.service.HouseSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/9/11

*/
@RestController
public class TestElasticSearchHouseController {

    @Autowired
    House2SearchService searchService;
    @Autowired
    HouseSearchService houseSearchService;

    @RequestMapping("getAll2")
    public Object getAll2(){
        return searchService.findAll();
    }

    @RequestMapping("getAll")
    public Object getAll(){
        return houseSearchService.findAll();
    }

}

