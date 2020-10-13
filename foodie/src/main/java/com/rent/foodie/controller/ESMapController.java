package com.rent.foodie.controller;

import com.rent.foodie.es.pojo.House;
import com.rent.foodie.es.pojo.Under;
import com.rent.foodie.mongo.pojo.Recommendation;
import com.rent.foodie.mongo.pojo.User;
import com.rent.foodie.service.HouseSearchService;
import com.rent.foodie.service.serviceimpl.MongoHouseService;
import com.rent.foodie.service.serviceimpl.MongoUserService;
import com.rent.foodie.service.serviceimpl.RecommenderService;
import com.rent.foodie.service.serviceimpl.UnderSearchService;
import org.elasticsearch.common.geo.GeoPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/20

*/
@RestController
@RequestMapping("/esmap")
public class ESMapController {


    @Autowired
    MongoHouseService houseService;
    @Autowired
    MongoUserService userService;
    @Autowired
    HouseSearchService houseSearchService;

    @Autowired
    UnderSearchService underSearchService;
    @Autowired
    RecommenderService recommenderService;


    @RequestMapping("/hello")
    public Object hello(HttpServletRequest request,HttpServletResponse response){
        return "hello psingboot!";
    }

    @RequestMapping("/geoDistance")
    public Object geoDistance(@RequestBody GeoPoint centerPoint,HttpServletRequest request){
        System.out.println("默认未登录接收到的点坐标："+ centerPoint);
        List<House> goodsList = new ArrayList<>();
        Set<Under> unders = new LinkedHashSet<>();
        String user = (String)request.getSession().getAttribute("longinuser");
        if (user != null){
            List<House> tmp = new ArrayList<>();
            List<com.rent.foodie.mongo.pojo.House> guessHouses = getGuessHouses(user, 500);
            List<Integer> ids = new ArrayList<>();
            for (com.rent.foodie.mongo.pojo.House guessHouse : guessHouses) {
                ids.add(guessHouse.getHid());
                tmp.add(houseSearchService.findById(ids + "").get());
            }
            for (House house : tmp) {
                if ( getDistance(house,5,centerPoint)!=null){
                    goodsList.add(getDistance(house,5,centerPoint));
                }
            }
            System.out.println("已登录用户的列表推荐:"+Arrays.asList(goodsList));
            return goodsList;
        }
        //解析聚合结果
        Iterable<House> all = houseSearchService.findAll();
        Iterator<House> iterator = all.iterator();
        while (iterator.hasNext()){
            House next = iterator.next();
            GeoPoint point = new GeoPoint(next.getLai(),next.getLon());
           if ( getDistance(next,3,centerPoint)!=null){
               goodsList.add(getDistance(next,3,centerPoint));
               unders.add(nearest(point));
           }
        }
        System.out.println("未登录用户的列表推荐:"+Arrays.asList(goodsList));
        return goodsList;
    }

    @RequestMapping("/lgeoDistance")
    public Object lgeoDistance(@RequestBody GeoPoint centerPoint,HttpServletRequest request){
        System.out.println("离线推荐接收到的点坐标："+ centerPoint);
        List<House> goodsList = new ArrayList<>();
        Set<Under> unders = new LinkedHashSet<>();
        String user = (String)request.getSession().getAttribute("loginuser");
        List<House> tmp = new ArrayList<>();
        List<com.rent.foodie.mongo.pojo.House> guessHouses = getWishHouses(500, user);
        for (com.rent.foodie.mongo.pojo.House guessHouse : guessHouses) {
            tmp.add(houseSearchService.findById(guessHouse.getHid() + "").get());
            }
        for (House house : tmp) {
            if ( getDistance(house,5,centerPoint)!=null){
                goodsList.add(getDistance(house,5,centerPoint));
            }
        }
        System.out.println("已登录用户的列表推荐："+Arrays.asList(goodsList));
        return goodsList;
    }
    @RequestMapping("/GgeoDistance")
    public Object GgeoDistance(@RequestBody GeoPoint centerPoint,HttpServletRequest request){
        System.out.println("实时推荐接收到的点坐标："+ centerPoint);
        List<House> goodsList = new ArrayList<>();
        Set<Under> unders = new LinkedHashSet<>();
        String user = (String)request.getSession().getAttribute("loginuser");
        List<House> tmp = new ArrayList<>();
        List<com.rent.foodie.mongo.pojo.House> guessHouses = getGuessHouses(user, 500);
        for (com.rent.foodie.mongo.pojo.House guessHouse : guessHouses) {
            tmp.add(houseSearchService.findById(guessHouse.getHid() + "").get());
        }
        for (House house : tmp) {
            if ( getDistance(house,10,centerPoint)!=null){
                goodsList.add(getDistance(house,10,centerPoint));
            }
        }
        System.out.println("已登录用户的实时列表推荐："+Arrays.asList(goodsList));
        return goodsList;
    }
    @RequestMapping("/ngeoDistance")
    public Object ngeoDistance(@RequestBody GeoPoint centerPoint,HttpServletRequest request){
        System.out.println("基于内容接收到的点坐标："+ centerPoint);
        List<House> goodsList = new ArrayList<>();
        String user = (String)request.getSession().getAttribute("loginuser");
        List<House> tmp = new ArrayList<>();
        List<com.rent.foodie.mongo.pojo.House> guessHouses = getContentBasedHouse(500, user);
        for (com.rent.foodie.mongo.pojo.House guessHouse : guessHouses) {
            tmp.add(houseSearchService.findById(guessHouse.getHid() + "").get());
        }
        for (House house : tmp) {
            if ( getDistance(house,5,centerPoint)!=null){
                goodsList.add(getDistance(house,5,centerPoint));
            }
        }
        System.out.println("新登录用户的列表推荐："+Arrays.asList(goodsList));
        return goodsList;
    }

    @RequestMapping("/hotPlace")
    public Object getHotPlace(@RequestBody GeoPoint centerPoint){
        List<House> goodsList = new ArrayList<>();
        List<House> tmp = new ArrayList<>();
        List<com.rent.foodie.mongo.pojo.House> hotHouses = getHotHouses(500);
        for (com.rent.foodie.mongo.pojo.House hotHouse : hotHouses) {
            tmp.add(houseSearchService.findById(hotHouse.getHid() + "").get());
        }
        for (House house : tmp) {
            if ( getDistance(house,5,centerPoint)!=null){
                goodsList.add(getDistance(house,5,centerPoint));
            }
        }
        System.out.println("浏览数量较多的列表："+Arrays.asList(goodsList));
        return goodsList;
    }

    @RequestMapping("/underDistance")
    public Under getUnder(@RequestBody GeoPoint centerPoint){
        return nearest(centerPoint);
    }

    public House getDistance(House next,int km, GeoPoint centerPoint){
        GeoPoint point = new GeoPoint(next.getLai(),next.getLon());
        double d = getLatLngDistance(centerPoint,point);
        String format = String.format("%.2f", d);
        if (Double.parseDouble(format)<=km){
            com.rent.foodie.mongo.pojo.House mongohouse = houseService.findByMID(next.getPid());
            next.setHouseSingleUrl(mongohouse.getHouseSingleUrl());
            next.setPrice(mongohouse.getPrice());
            next.setDirectType(mongohouse.getDirectType());
            next.setRentType(mongohouse.getRentType());
            next.setSingleLayer(mongohouse.getSingleLayer());
            next.setSize(mongohouse.getSize());
            next.setRentType(mongohouse.getRentType());
            next.setPlace(mongohouse.getPlace());
            next.setSingleType(mongohouse.getSingleType());
            next.setDis(format);
            return next;
        }
        return null;
    }
    //获取热门房源的方法
    public List<com.rent.foodie.mongo.pojo.House> getHotHouses(int num){
        List<Recommendation> recommendations = recommenderService.getHotRecommendation(num);
        return houseService.getRecommendHouses(recommendations);
    }


    //离线的个人推荐
    public List<com.rent.foodie.mongo.pojo.House> getWishHouses(int num, String name){
        User loginuser = userService.findByUsername(name);
        List<Recommendation> recommendations = recommenderService.findUserCFRecs(loginuser.getUid(), num);
        if (recommendations.size() == 0){
            String randomGenres = loginuser.getPrefGenres().get(new Random().nextInt(loginuser.getPrefGenres().size()));
            recommendations = recommenderService.getTopTypesRecommendations(num, randomGenres.split(" ")[0]);
        }
        return houseService.getRecommendHouses(recommendations);
    }
    //新登录的用户基于选择的房型推荐
    public List<com.rent.foodie.mongo.pojo.House> getContentBasedHouse(int num, String name){
        User loginuser = userService.findByUsername(name);
        List<Recommendation> recommendations = new ArrayList<>();
        for (String prefGenre : loginuser.getPrefGenres()) {
            for (Recommendation topTypesRecommendation : recommenderService.getTopTypesRecommendations(50, prefGenre)) {
                recommendations.add(topTypesRecommendation);
            }
        }
        if (recommendations.size() == 0){
            String randomGenres = loginuser.getPrefGenres().get(new Random().nextInt(loginuser.getPrefGenres().size()));
            recommendations = recommenderService.getTopTypesRecommendations(num, randomGenres.split(" ")[0]);
        }
        return houseService.getRecommendHouses(recommendations);
    }

    //实时推荐的方法
    public List<com.rent.foodie.mongo.pojo.House> getGuessHouses(String name, int num){
        User user = userService.findByUsername(name);
        List<Recommendation> recommendations = recommenderService.findHybridRecommendations(user.getUid(), num);
        if (recommendations.size() == 0){
            String randomGenres = user.getPrefGenres().get(new Random().nextInt(user.getPrefGenres().size()));
            recommendations = recommenderService.getTopTypesRecommendations(num, randomGenres.split(" ")[0]);
        }
        return houseService.getHybirdRecommendHouses(recommendations);
    }

    public double getLatLngDistance(GeoPoint start, GeoPoint end){
        //自己实现距离算法：
        /**
         * 计算两点之间距离
         * @param start
         * @param end
         * @return String  多少m ,  多少km
         */
        double lat1 = (Math.PI/180)*start.lat();
        double lat2 = (Math.PI/180)*end.lat();

        double lon1 = (Math.PI/180)*start.lon();
        double lon2 = (Math.PI/180)*end.lon();

        //地球半径
        double R = 6371.004;

        //两点间距离 m，如果想要米的话，结果*1000就可以了
        double dis =  Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon2-lon1))*R;
        double dis2 =  Math.acos(Math.sin(lat1)*Math.sin(lat2)+Math.cos(lat1)*Math.cos(lat2)*Math.cos(lon1)*Math.cos(lon2)
        +Math.cos(lat1)*Math.sin(lon1)*Math.cos(lat2)*Math.sin(lon2))*R;
        return dis2;
    }


    public Under nearest(GeoPoint centerPoint){
        Iterable<Under> all = underSearchService.findAll();
        Iterator<Under> iterator = all.iterator();
        double min = 9999999;
        int pid = 0;
        while (iterator.hasNext()){
            Under under = iterator.next();
            GeoPoint point = new GeoPoint(under.getLai(),under.getLon());
            if (getLatLngDistance(centerPoint,point)<=min){
                  min = getLatLngDistance(centerPoint,point);
                  pid = under.getPid();
            }
        }
       return underSearchService.findById(pid+"").get();
    }
}
