package com.rent.foodie.service.serviceimpl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import com.rent.foodie.mongo.pojo.Constant;
import com.rent.foodie.mongo.pojo.House;
import com.rent.foodie.mongo.pojo.Recommendation;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/28

*/
@Service
public class MongoHouseService {
    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> houseCollection;


    private MongoCollection<Document> getHouseCollection(){
        if(null == houseCollection)
            houseCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_HOUSE_COLLECTION);
        return houseCollection;
    }

    public House findByMID(int hid){
        Document document = getHouseCollection().find(new Document("hid",hid)).first();
        if(document == null || document.isEmpty())
            return null;
        return documentToHouse(document);
    }

    public List<House> getHybirdRecommendHouses(List<Recommendation> recommendations){
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            ids.add(recommendation.getHid());
        }
        return getHouses(ids);
    }

    public List<House> getRecommendHouses(List<Recommendation> recommendations){
        List<Integer> ids = new ArrayList<>();
        for (Recommendation recommendation : recommendations) {
            ids.add(recommendation.getHid());
        }
        return getHouses(ids);
    }

    public List<House> getHouses(List<Integer> hids) {
        FindIterable<Document> documents = getHouseCollection().find(Filters.in("hid", hids));
        List<House> houses = new ArrayList<>();
        for (Document document : documents) {
            houses.add(documentToHouse(document));
        }
        return houses;
    }

    public List<House> getAll(){
        List<House> rs = new ArrayList<>();
        FindIterable<Document> documents = getHouseCollection().find();
        for (Document document : documents) {
            rs.add(documentToHouse(document));
        }
        String path = "D:\\house.csv";
        StringBuffer buffer = new StringBuffer();
        System.out.println(rs.size());
        for (House r : rs) {
            String s = r.getHid() + ","+r.getTitle().replace(","," ")+","+r.getSingleType().replace(","," ")+","+r.getSize().replace(","," ")+","+r.getDirectType().replace(","," ")+","+r.getSingleLayer().replace(","," ")
                    +","+r.getRentType().replace(","," ")+","+r.getPlace().replace(","," ")+","+r.getUnderPlace().replace(","," ")+","+r.getSchoolPlace().replace(","," ")+","+r.getPrice()+"\n";
            buffer.append(s);
        }

        try {
            FileOutputStream fop = new FileOutputStream(path);
            OutputStreamWriter writer = new OutputStreamWriter(fop,"UTF-8");
            writer.append(buffer);
            writer.close();
            fop.close();
        } catch (FileNotFoundException e) {

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rs;
    }

    private House documentToHouse(Document document){
        House house = null;
        try{
            house = objectMapper.readValue(JSON.serialize(document),House.class);
        }catch (IOException e) {
            e.printStackTrace();
        }
        return house;
    }
}
