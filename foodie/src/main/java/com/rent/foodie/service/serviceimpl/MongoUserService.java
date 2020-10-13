package com.rent.foodie.service.serviceimpl;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.util.JSON;
import com.rent.foodie.mongo.pojo.Constant;
import com.rent.foodie.mongo.pojo.User;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/25

*/
@Service
public class MongoUserService {
    @Autowired
    private MongoClient mongoClient;
    @Autowired
    private ObjectMapper objectMapper;

    private MongoCollection<Document> userCollection;
    private MongoCollection<Document> getUserCollection(){
        if(null == userCollection)
            userCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_COLLECTION);
        return userCollection;
    }

    public User loginUser(String username) throws JsonProcessingException {
        Document user = getUserCollection().find(new Document("account", username)).first();
        return documentToUser(user);
    }

    public boolean registerUser(User user) {
        try {
            getUserCollection().insertOne(Document.parse(objectMapper.writeValueAsString(user)));
            return true;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateUser(User user){
        getUserCollection().updateOne(Filters.eq("uid", user.getUid()), new Document().append("$set",new Document("prefGenres", user.getPrefGenres())));
        getUserCollection().updateOne(Filters.eq("uid",user.getUid()),new Document().append("$set",new Document("first",user.getFirst())));
        return true;
    }

    public boolean setUserHistory(User user){
        getUserCollection().updateOne(Filters.eq("uid",user.getUid()),new Document().append("$set",new Document("history",user.getHistory())));
        return true;
    }

    public boolean checkUserExist(String username){
        return null != findByUsername(username);
    }

    public User findByUsername(String username){
        Document user = getUserCollection().find(new Document("name",username)).first();
        if(null == user || user.isEmpty())
            return null;
        return documentToUser(user);
    }

    public void ChangeUserRecord(User user){

    }
    private User documentToUser(Document document){
        try{
            return objectMapper.readValue(JSON.serialize(document),User.class);
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
