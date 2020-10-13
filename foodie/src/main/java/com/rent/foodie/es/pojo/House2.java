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
@Document(indexName = "house",type = "house",createIndex = false)
public class House2 {

    @Id
    private String id;

    @Field
    private int hid;
    @Field
    private String title;
    @Field
    private String singleType;
    @Field
    private String size;
    @Field
    private String directType;
    @Field
    private String singleLayer;
    @Field
    private String rentType;
    @Field
    private String place;
    @Field
    private String underPlace;
    @Field
    private String schoolPlace;
    @Field
    private int price;

}
