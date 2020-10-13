package com.rent.foodie.mongo.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/28

*/
@Data
public class House {
    @JsonIgnore
    private String _id;

    private int hid;

    private String houseSingleUrl;

    private String title;

    private String singleType;

    private String size;

    private String directType;

    private String singleLayer;

    private String rentType;

    private String place;

    private String underPlace;

    private String schoolPlace;

    private int price;
}
