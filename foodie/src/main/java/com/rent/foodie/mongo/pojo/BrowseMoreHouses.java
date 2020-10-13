package com.rent.foodie.mongo.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/28
这表示的是有多少个用户浏览了该房子
*/
@Data
public class BrowseMoreHouses {
    @JsonIgnore
    private String _id;

    private int hid;

    private int count;
}
