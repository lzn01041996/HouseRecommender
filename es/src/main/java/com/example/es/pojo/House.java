package com.example.es.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/22

*/
@Document(indexName = "houserec",type = "_doc")
public class House {

    @Id
    private String id;

    @Field
    private int pid;

    @Field
    private String name;

    @Field
    private double lon;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLai() {
        return lai;
    }

    public void setLai(double lai) {
        this.lai = lai;
    }

    @Field
    private double lai;

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}
