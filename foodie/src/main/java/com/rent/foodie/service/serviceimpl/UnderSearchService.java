package com.rent.foodie.service.serviceimpl;

import com.rent.foodie.es.pojo.House;
import com.rent.foodie.es.pojo.Under;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/22

*/
@Service
public interface UnderSearchService extends ElasticsearchRepository<Under,String> {
    //List<House> findAllHouse();
}
