package com.rent.foodie.es.pojo;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/20

*/
@Data
@Document(indexName = "underec",type = "_doc",createIndex = false)
public class Under {

    @Id
    private String id;
    @Field
    private int pid;
    @Field
    private String name;
    @Field
    private double lon;
    @Field
    private double lai;
}
