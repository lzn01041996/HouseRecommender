package com.rent.foodie.controller;

import com.rent.foodie.mongo.pojo.House;
import com.rent.foodie.mongo.pojo.Recommendation;
import com.rent.foodie.mongo.pojo.User;
import com.rent.foodie.service.serviceimpl.MongoHouseService;
import com.rent.foodie.service.serviceimpl.MongoUserService;
import com.rent.foodie.service.serviceimpl.RecommenderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Random;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/28
关于推荐的所有内容的接口
*/
@RestController
@RequestMapping("/house")
public class MongoHouseController {

    @Autowired
    MongoHouseService houseService;
    @Autowired
    MongoUserService userService;
    @Autowired
    RecommenderService recommenderService;

    //实时推荐，是混合推荐推荐的结果是6个实时推荐的内容+4个基于内容推荐的内容
    @RequestMapping("/guess")
    public List<House> getGuessHouses(HttpServletRequest request, @RequestParam("num")int num){
        User user = userService.findByUsername("一只大西瓜"/*(String) request.getSession().getAttribute("loginuser")*/);
        List<Recommendation> recommendations = recommenderService.findHybridRecommendations(user.getUid(), num);
        if (recommendations.size() == 0){
            String randomGenres = user.getPrefGenres().get(new Random().nextInt(user.getPrefGenres().size()));
            recommendations = recommenderService.getTopTypesRecommendations(num, randomGenres.split(" ")[0]);
        }
        return houseService.getHybirdRecommendHouses(recommendations);
    }
    //离线推荐的数据
    @RequestMapping("/wish")
    public List<House> getWishHouses(@RequestParam("num") int num,HttpServletRequest request){
        User loginuser = userService.findByUsername("一只大西瓜"/*(String) request.getSession().getAttribute("loginuser")*/);
        List<Recommendation> recommendations = recommenderService.findUserCFRecs(loginuser.getUid(), num);
        if (recommendations.size() == 0){
            String randomGenres = loginuser.getPrefGenres().get(new Random().nextInt(loginuser.getPrefGenres().size()));
            recommendations = recommenderService.getTopTypesRecommendations(num, randomGenres.split(" ")[0]);
        }
        return houseService.getRecommendHouses(recommendations);
    }

    //热门的房源,也就是浏览次数最多的房子
    @RequestMapping("/hot")
    public List<House> getHotHouses(@RequestParam("num") int num){
        List<Recommendation> recommendations = recommenderService.getHotRecommendation(num);
        return houseService.getRecommendHouses(recommendations);
    }

    @RequestMapping("/info/{hid}")
    public House getHouseInfo(@PathVariable("hid")int id, Model model) {
        return houseService.findByMID(id);
    }

    @RequestMapping("/all")
    public List<House> getAll(){
        return houseService.getAll();
    }

}
