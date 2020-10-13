package com.rent.foodie.mongo.pojo;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/25

*/
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;


/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/21

*/
@Data
public class User {
    @JsonIgnore
    private String _id;

    private int uid;

    private String name;

    private String account;

    private String password;

    private String first;

    private List<String> prefGenres = new ArrayList<>();

    private List<String> history = new ArrayList<>();


}

