package com.rent.foodie.mongo.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.List;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/28

*/
@Data
public class HouseRecs {

    @JsonIgnore
    private String _id;

    private int hid;

    private List<Recommendation> recs;
}
