package com.rent.foodie.service;

import com.rent.foodie.es.pojo.House;
import com.rent.foodie.es.pojo.House2;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/22

*/
@Service
public interface House2SearchService extends ElasticsearchRepository<House2,String> {
    //List<House> findAllHouse();
}
