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
public class Rec {


    private String id;

    private int pid;

    private String name;

    private double lon;

    private double lai;
}
