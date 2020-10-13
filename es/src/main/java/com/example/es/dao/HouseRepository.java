package com.example.es.dao;

import com.example.es.pojo.House;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Component;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/22

*/
@Component
public interface HouseRepository extends ElasticsearchRepository<House,String> {
}
