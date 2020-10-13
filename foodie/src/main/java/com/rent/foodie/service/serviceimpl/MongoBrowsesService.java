package com.rent.foodie.service.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import com.rent.foodie.mongo.pojo.*;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/9/1

*/
@Service
public class MongoBrowsesService {

    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    Jedis jedis;


    private MongoCollection<Document> browseCollection;

    private MongoCollection<Document> getBrowsesCollection() {
        if (null == browseCollection)
            browseCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_BROWSES_COLLECTION);
        return browseCollection;
    }

    private MongoCollection<Document> getAverageBrowseCollection() {
        if (null == browseCollection)
            browseCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_AVERAGE_BROWSE_COLLECTION);
        return browseCollection;
    }

    public boolean houseBrowsesAfter(Browses browse){
        String distanceTime = getDistanceTime(browse.getEndTime(), browse.getStartTime());
        if (Integer.parseInt(distanceTime)<=3600 && Integer.parseInt(distanceTime)>=10){
            updateRedis(browse);
        }
        return updateBrowsesAfter(browse);

    }

    public boolean houseBrowsesBefore(Browses browse){
        Browses browse1 = new Browses(browse.getUid(),browse.getHid(),browse.getTimes(),
                browse.getStartTime(),browse.getEndTime());
        if (browseExist(browse1.getUid(),browse1.getHid())){
            return updateBrowsesBefore(browse1);
        }else {
            return newBrowses(browse1);
        }
    }

    private void updateRedis(Browses browse){
        if (jedis.exists("uid:" + browse.getUid()) && jedis.llen("uid:" + browse.getUid()) >= Constant.REDIS_HOUSE_RATING_QUEUE_SIZE){
            jedis.rpop("uid:" + browse.getUid());
        }
        jedis.lpush("uid:"+browse.getUid(),browse.getHid() + ":" + browse.getTimes());
    }

    public boolean newBrowses(Browses browse){
        try {
            getBrowsesCollection().insertOne(Document.parse(objectMapper.writeValueAsString(browse)));
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean browseExist(int uid,int hid){
        return null !=findBrowses(uid,hid);
    }

    public boolean updateBrowsesBefore(Browses browse){

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("uid", browse.getUid());
        basicDBObject.append("hid", browse.getHid());
        int i = findBrowses(browse.getUid(), browse.getHid()).getTimes();
        getBrowsesCollection().updateOne(basicDBObject,new Document().append("$set",new Document("times",i))
                .append("$set",new Document("startTime",browse.getStartTime())));
        return true;
    }

    public boolean updateBrowsesAfter(Browses browse){

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("uid", browse.getUid());
        basicDBObject.append("hid", browse.getHid());
        int i = findBrowses(browse.getUid(), browse.getHid()).getTimes() + 1;
        getBrowsesCollection().updateOne(basicDBObject,new Document().append("$set",new Document("times",i))
                .append("$set",new Document("endTime",browse.getStartTime())));
        return true;
    }

    public boolean updateAverageBrowse(Browse browse){
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("hid",browse.getHid());
       if (findAverageBrowse(browse.getHid()) == null){
           try {
               getAverageBrowseCollection().insertOne(Document.parse(objectMapper.writeValueAsString(new AverageBrowse(browse.getHid(),1))));
           } catch (JsonProcessingException e) {
               e.printStackTrace();
           }

       }else{
           int i = findAverageBrowse(browse.getHid()).getSum() + 1;
           getAverageBrowseCollection().updateOne(basicDBObject,new Document().append("$set",new Document("sum",i)));
       }
        return true;
    }

    public Browses findBrowses(int uid,int hid){
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("uid",uid);
        basicDBObject.append("hid",hid);
        FindIterable<Document> documents = getBrowsesCollection().find(basicDBObject);
        if (documents.first() == null) {
            return null;
        }
        return documentToBrowse(documents.first());
    }
    public AverageBrowse findAverageBrowse(int hid){
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("hid",hid);
        FindIterable<Document> documents = getAverageBrowseCollection().find(basicDBObject);
        if (documents.first() == null){
            return null;
        }
        return documentToAverageBrowse(documents.first());
    }

    public void removeBrowse(int uid,int hid){
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("uid",uid);
        basicDBObject.append("hid",hid);
        getBrowsesCollection().deleteOne(basicDBObject);
    }

    public int[] getMyBrowsesStat(User user){
        FindIterable<Document> documents = getBrowsesCollection().find(new Document("uid",user.getUid()));
        int[] stats = new int[10];
        for (Document document : documents) {
            Browses browse = documentToBrowse(document);
            Long index = Math.round(browse.getTimes()/0.5);
            stats[index.intValue()] = stats[index.intValue()] + 1;
        }
        return stats;
    }

    private Browses documentToBrowse(Document document) {
        Browses browse = null;
        try {
            browse = objectMapper.readValue(JSON.serialize(document), Browses.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return browse;
    }

    private AverageBrowse documentToAverageBrowse(Document document) {
        AverageBrowse browse = null;
        try {
            browse = objectMapper.readValue(JSON.serialize(document), AverageBrowse.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return browse;
    }

    public static String getDistanceTime(long time1, long time2) {
        long day = 0;
        long hour = 0;
        long min = 0;
        long sec = 0;
        long diff;

        if (time1 < time2) {
            diff = time2 - time1;
        } else {
            diff = time1 - time2;
        }
        day = diff / (24 * 60 * 60 * 1000);
        hour = (diff / (60 * 60 * 1000) - day * 24);
        min = ((diff / (60 * 1000)) - day * 24 * 60 - hour * 60);
        sec = (diff / 1000 - day * 24 * 60 * 60 - hour * 60 * 60 - min * 60);
        if (day != 0) return day*24*60*60 +hour*60*60 +min*60 + sec + "";
        if (hour != 0) return hour*60*60 +  min*60 +  sec + "";
        if (min != 0) return min*60 +sec + "";
        if (sec != 0) return sec + "" ;
        return "0秒";
    }
}
