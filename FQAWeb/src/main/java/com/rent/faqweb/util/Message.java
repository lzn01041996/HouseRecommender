package com.rent.faqweb.util;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/12/16

*/
public class Message {

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isMine() {
        return mine;
    }

    public void setMine(boolean mine) {
        this.mine = mine;
    }

    //"username":"纸飞机","avatar":"images/5.pic.jpg","id":1,"mine":true,"content":"如何减肥"
    private String avatar;
    private int id;
    private boolean mine;
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
