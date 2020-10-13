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
public class Browses {


    @JsonIgnore
    private String _id;

    private int uid;
    private int hid;
    private int times;
    private long startTime;
    private long endTime;

    public Browses(int uid, int hid, int times,long startTime,long endTime){
        this.uid = uid;
        this.hid = hid;
        this.times = times;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
