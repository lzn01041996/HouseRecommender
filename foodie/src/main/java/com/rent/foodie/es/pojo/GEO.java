package com.rent.foodie.es.pojo;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/21

*/
import org.springframework.data.elasticsearch.annotations.Document;

@Document(indexName = "geo", type = "_doc", createIndex = false)
public class GEO {

    private double lon;
    private double lat;

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    @Override
    public String toString() {
        return "GEO{" +
                "lon=" + lon +
                ", lat=" + lat +
                '}';
    }
}
