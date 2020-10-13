package com.rent.foodie.mongo.pojo;

import lombok.Data;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/28

*/
@Data
public class Recommendation {

    private int hid;

    private Double count;

    public Recommendation(int hid, Double count) {
        this.hid = hid;
        this.count = count;
    }
}
