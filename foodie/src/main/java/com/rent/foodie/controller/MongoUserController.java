package com.rent.foodie.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rent.foodie.mongo.pojo.User;
import com.rent.foodie.service.serviceimpl.MongoUserService;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/25

*/
@RestController
@RequestMapping("/user")
public class MongoUserController {
    @Autowired
    MongoUserService userService;

    @RequestMapping("/login")
    public String longin(HttpServletRequest request, @RequestParam("username") String username,
                         @RequestParam("password") String password) throws JsonProcessingException {
        User user = userService.loginUser(username);
        if (user==null){
            return "fail";
        }
        if (!user.getPassword().equals(password)){
            return "fail";
        }
        request.getSession().setAttribute("loginuser",user.getName());
        System.out.println("登录进来的用户名："+username);
        if (user.getFirst() == null){
            user.setFirst("false");
            userService.updateUser(user);
        }
       return username+","+user.getFirst();
    }

    @RequestMapping("/register")
    public String addUser(@RequestParam("username") String username, @RequestParam("password") String password,
                         @RequestParam("name") String name, Model model) {
        if(userService.checkUserExist(username)){
            model.addAttribute("success",false);
            model.addAttribute("message"," 用户名已经被注册！");
            return "fail";
        }
        User user = new User();
        long l = System.currentTimeMillis();
        user.setUid((int) l);
        user.setAccount(username);
        user.setFirst("true");
        user.setName(name);
        user.setPassword(password);
        model.addAttribute("success",userService.registerUser(user));
        return "success";
    }

    //冷启动问题
    @RequestMapping("/pref")
    public String addPrefGenres(@RequestParam("genres") String genres,HttpServletRequest request) {
        String username = (String) request.getSession().getAttribute("loginuser");
        User user = userService.findByUsername(username);
        user.setFirst("true");
        user.getPrefGenres().addAll(Arrays.asList(genres.split(",")));
        if (userService.updateUser(user)){
            return "success" ;
        }
        return "fail";
    }

    @RequestMapping("/search")
    public String searchRecord(@RequestParam("title")String title,@RequestParam("lat") String lat,@RequestParam("lon") String lon, HttpServletRequest request){
        String username = (String) request.getSession().getAttribute("loginuser");
        User user = userService.findByUsername(username);
        String s = title + "," + lat + "," + lon;
        user.getHistory().addAll(Collections.singleton(s));
        if (userService.setUserHistory(user)){
            return "success";
        }
        return "fail";
    }

    @RequestMapping("/history")
    public Object getHisory(HttpServletRequest request){
        String username = (String) request.getSession().getAttribute("loginuser");
        User user = userService.findByUsername(username);
        List<String> history = user.getHistory();
        String[] split = history.get(history.size() - 1).split(",");
        double lon = Double.parseDouble(split[1]);
        double lat = Double.parseDouble(split[2]);
        Map p = new HashMap<String,Double>();
        List<Double> list = new ArrayList<>();
        list.add(lat);list.add(lon);
        return list;
    }

}
