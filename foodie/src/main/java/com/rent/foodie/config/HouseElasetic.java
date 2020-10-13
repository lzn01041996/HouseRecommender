package com.rent.foodie.config;

import com.rent.foodie.es.pojo.User;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.ElasticsearchCrudRepository;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/21

*/
@Configuration
public interface HouseElasetic extends ElasticsearchCrudRepository<User,Integer> {
}
