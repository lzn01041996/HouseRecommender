package com.rent.foodie.controller;

import com.rent.foodie.es.pojo.House;
import com.rent.foodie.mongo.pojo.Browse;
import com.rent.foodie.mongo.pojo.Browses;
import com.rent.foodie.mongo.pojo.Constant;
import com.rent.foodie.mongo.pojo.User;
import com.rent.foodie.service.House2SearchService;
import com.rent.foodie.service.HouseSearchService;
import com.rent.foodie.service.serviceimpl.MongoBrowseService;
import com.rent.foodie.service.serviceimpl.MongoBrowsesService;
import com.rent.foodie.service.serviceimpl.MongoHouseService;
import com.rent.foodie.service.serviceimpl.MongoUserService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/20

*/
@Controller
public class PageController {

    private static Logger logger = Logger.getLogger(PageController.class.getName());

    @Autowired
    MongoHouseService houseService;

    @Autowired
    MongoUserService userService;

    @Autowired
    MongoBrowseService browseService;

    @Autowired
    MongoBrowsesService browsesService;

    @Autowired
    HouseSearchService houseSearchService;

    @RequestMapping("/index")
    public String getIndex(){
        return "index";
    }

    @RequestMapping("/test")
    public String getEt(){
        return "test";
    }

    @RequestMapping("/user_login")
    public String login(){
        return "login";
    }
    @RequestMapping("/user_register")
    public String register(){
        return "register";
    }
    @RequestMapping("/logedIndex")
    public String loged(){
        return "logedIndex";
    }
    @RequestMapping("/logedIndexs")
    public String logeds(HttpServletRequest request){
        String loginuser =(String) request.getSession().getAttribute("loginuser");
        int pid =(int) request.getSession().getAttribute("hid");
        User user = userService.findByUsername(loginuser);
        Browses browses = browsesService.findBrowses(user.getUid(), pid);
        browses.setEndTime(System.currentTimeMillis());
        browsesService.houseBrowsesAfter(browses);
        System.out.println(browses);
        //过滤数据，浏览时间过长或者过短的数据将不会录入
        String distanceTime = getDistanceTime(browses.getEndTime(), browses.getStartTime());
        if (Integer.parseInt(distanceTime)<=3600 && Integer.parseInt(distanceTime)>=10){
            //用户浏览的房源加入记录，并且将该位置计入history
            House house = houseSearchService.findById(pid + "").get();
            String s = house.getName() + "," + house.getLai() + "," + house.getLon();
            user.getHistory().addAll(Collections.singleton(s));
            userService.setUserHistory(user);
            //埋点日志****************************************
            System.out.println("------------complete------------");
            logger.info(Constant.HOUSE_BROWSE_PREFIX + ":" + user.getUid()+ "|" +pid + "|" + browses.getTimes());
        }

        return "logedIndex";
    }
    @RequestMapping("/FirstLindex")
    public String firstLog(){return "FirstLindex";}
    @RequestMapping("/house_detail")
    public String returnDetail(@RequestParam("pid") int pid,Model model,HttpServletRequest request){
        String loginuser =(String) request.getSession().getAttribute("loginuser");
        if (loginuser == null){
            return "login";
        }
        User user = userService.findByUsername(loginuser);
        user.setFirst("false");
        userService.updateUser(user);
        Browse browse = new Browse(user.getUid(),pid,1);
        Browses browses = new Browses(user.getUid(),pid,1,System.currentTimeMillis(),0);
        browsesService.houseBrowsesBefore(browses);
        boolean complate = browseService.houseBrowse(browse);
        browseService.updateAverageBrowse(browse);
        request.getSession().setAttribute("hid",pid);
        model.addAttribute("house",houseService.findByMID(pid));
        model.addAttribute("message","成功");
        return "single";
    }

    @RequestMapping("/user_logout")
    public String logout(HttpServletRequest request){
        request.getSession().removeAttribute("loginuser");
        return "index";
    }

    public static String getDistanceTime(long time1, long time2) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long diff;

        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        if (day != 0) return day*24*60*60 +hour*60*60 +min*60 + sec + "";
        if (hour != 0) return hour*60*60 +  min*60 +  sec + "";
        if (min != 0) return min*60 +sec + "";
        if (sec != 0) return sec + "" ;
        return "0秒";
    }
}
