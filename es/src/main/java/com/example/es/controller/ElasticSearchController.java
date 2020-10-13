package com.example.es.controller;

import com.example.es.dao.HouseRepository;
import com.example.es.pojo.House;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/22

*/
@RestController
@RequestMapping("/es")
public class ElasticSearchController {

    @Autowired
    private HouseRepository hs;

    @RequestMapping("/query/{pid}")
    public House query(@PathVariable("pid") String pid){
        Optional<House> byId = hs.findById(pid);
        return byId.get();
    }

    @RequestMapping("/get/{pid}")
    public List<House> get(@PathVariable("pid") String pid){
        Optional<House> byId = hs.findById(pid);
        House house = byId.get();
        List<House> houses = new ArrayList<>();
        return null;
    }

    @RequestMapping("/queryAll")
    public List<House> getAll(){
        Iterable<House> all = hs.findAll();
        Iterator<House> iterator = all.iterator();
        List<House> houses = new ArrayList<>();
        while (iterator.hasNext()){
            houses.add(iterator.next());
        }
        return houses;
    }
    @RequestMapping("/delete")
    public String delete(){
        hs.deleteById("994");
        return "success";
    }

    @RequestMapping("/add")
        public House add(){
        House house = new House();
        house.setId("22");
        house.setPid(22);
        house.setLon(152.23522);
        house.setLai(148.22521);
        house.setName("这是一个测试");
        return house;
    }
}
