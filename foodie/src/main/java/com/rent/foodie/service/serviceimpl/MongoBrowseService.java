package com.rent.foodie.service.serviceimpl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.util.JSON;
import com.rent.foodie.mongo.pojo.AverageBrowse;
import com.rent.foodie.mongo.pojo.Browse;
import com.rent.foodie.mongo.pojo.Constant;
import com.rent.foodie.mongo.pojo.User;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/9/1

*/
@Service
public class MongoBrowseService {

    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    Jedis jedis;


    private MongoCollection<Document> browseCollection;

    private MongoCollection<Document> getBrowseCollection() {
        if (null == browseCollection)
            browseCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_BROWSE_COLLECTION);
        return browseCollection;
    }

    private MongoCollection<Document> getAverageBrowseCollection() {
        if (null == browseCollection)
            browseCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_AVERAGE_BROWSE_COLLECTION);
        return browseCollection;
    }

    public boolean houseBrowse(Browse browse){
        Browse browse1 = new Browse(browse.getUid(),browse.getHid(),browse.getTimes());
        if (browseExist(browse1.getUid(),browse1.getHid())){
            return updateBrowse(browse1);
        }else {
            return newBrowse(browse1);
        }
    }

    private void updateRedis(Browse browse){
        if (jedis.exists("uid:" + browse.getUid()) && jedis.llen("uid:" + browse.getUid()) >= Constant.REDIS_HOUSE_RATING_QUEUE_SIZE){
            jedis.rpop("uid:" + browse.getUid());
        }
        jedis.lpush("uid:"+browse.getUid(),browse.getHid() + ":" + browse.getTimes());
    }

    public boolean newBrowse(Browse browse){
        try {
            getBrowseCollection().insertOne(Document.parse(objectMapper.writeValueAsString(browse)));
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean browseExist(int uid,int hid){
        return null !=findBrowse(uid,hid);
    }

    public boolean updateBrowse(Browse browse){

        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("uid", browse.getUid());
        basicDBObject.append("hid", browse.getHid());
        int i = findBrowse(browse.getUid(), browse.getHid()).getTimes() + 1;
        getBrowseCollection().updateOne(basicDBObject,new Document().append("$set",new Document("times",i)));
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

    public Browse findBrowse(int uid,int hid){
        BasicDBObject basicDBObject = new BasicDBObject();
        basicDBObject.append("uid",uid);
        basicDBObject.append("hid",hid);
        FindIterable<Document> documents = getBrowseCollection().find(basicDBObject);
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
        getBrowseCollection().deleteOne(basicDBObject);
    }

    public int[] getMyBrowseStat(User user){
        FindIterable<Document> documents = getBrowseCollection().find(new Document("uid",user.getUid()));
        int[] stats = new int[10];
        for (Document document : documents) {
            Browse browse = documentToBrowse(document);
            Long index = Math.round(browse.getTimes()/0.5);
            stats[index.intValue()] = stats[index.intValue()] + 1;
        }
        return stats;
    }

    private Browse documentToBrowse(Document document) {
        Browse browse = null;
        try {
            browse = objectMapper.readValue(JSON.serialize(document), Browse.class);
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
}
