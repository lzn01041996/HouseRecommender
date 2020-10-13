package com.rent.foodie.service;

import com.rent.foodie.es.pojo.House;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/22

*/
@Service
public interface HouseSearchService extends ElasticsearchRepository<House,String> {
    //List<House> findAllHouse();
}
