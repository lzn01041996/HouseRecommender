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
public class AverageBrowse {
    @JsonIgnore
    private String _id;
    private int hid;
    private int sum;

    public AverageBrowse(int hid,int sum){
        this.hid = hid;
        this.sum = sum;
    }
}
