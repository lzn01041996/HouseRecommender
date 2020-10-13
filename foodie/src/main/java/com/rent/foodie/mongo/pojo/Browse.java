package com.rent.foodie.mongo.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/28

*/
@Data
public class Browse {


    @JsonIgnore
    private String _id;

    private int uid;
    private int hid;
    private int times;

    public Browse(int uid,int hid,int times){
        this.uid = uid;
        this.hid = hid;
        this.times = times;
    }
}
