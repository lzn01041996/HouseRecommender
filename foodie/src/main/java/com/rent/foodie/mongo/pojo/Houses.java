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
public class Houses {
    @JsonIgnore
    private String _id;

    private int hid;

    private String singleType;

    private double size;

    private int directType;

    private int layer;

    private int rentType;

    private String underPlace;

    private String schoolPlace;

    private int price;
}
