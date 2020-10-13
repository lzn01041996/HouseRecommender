package com.rent.foodie.service.serviceimpl;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.rent.foodie.es.pojo.House2;
import com.rent.foodie.mongo.pojo.Constant;
import com.rent.foodie.mongo.pojo.Recommendation;
import com.rent.foodie.service.House2SearchService;
import com.rent.foodie.service.HouseSearchService;
import org.bson.Document;
import org.elasticsearch.index.query.FuzzyQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/9/2

*/
@Service
public class RecommenderService {
    //混合推荐中CF的比例
    private static Double CF_RATING_FACTOR = 0.3;
    private static Double CB_RATING_FACTOR = 0.3;
    private static Double SR_RATING_FACTOR = 0.4;

    @Autowired
    MongoClient mongoClient;
    //es的house搜索
    @Autowired
    HouseSearchService houseSearchService;

    @Autowired
    House2SearchService house2SearchService;

    //协同过滤的推荐，【房源相似性】
    public List<Recommendation> findHouseCFRecs(int hid,int maxItems){
        MongoCollection<Document> houseRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_HOUSE_RECS_COLLECTION);
        Document houseRecs = houseRecsCollection.find(new Document("hid", hid)).first();
        return parseRecs(houseRecs,maxItems);
    }
    //协同过滤推荐，【用户房源矩阵】
    public List<Recommendation> findUserCFRecs(int uid,int maxItems){
        MongoCollection<Document> houseRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_USER_RECS_COLLECTION);
        Document userRecs = houseRecsCollection.find(new Document("uid", uid)).first();
        return parseRecs(userRecs,maxItems);
    }
    //基于内容的推荐算法
    public List<Recommendation> findContentBasedMoreLikeRecommendations(int hid,int maxItems){
        MongoCollection<Document> houseRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_CONTENT_HOUSE_RECS_COLLECTION);
        Document houseRecs = houseRecsCollection.find(new Document("hid", hid)).first();
        return parseRecs(houseRecs,maxItems);
    }

    //实时推荐的算法
    public List<Recommendation> findStreamRecs(int uid,int maxItems){
        MongoCollection<Document> streamRecsCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_STREAM_RECS_COLLECTION);
        Document streamRecs = streamRecsCollection.find(new Document("uid", uid)).first();
        return parseRecs(streamRecs,maxItems);
    }

    //混合推荐算法
    public List<Recommendation> findHybridRecommendations(int prductId,int maxItems){
        List<Recommendation> hybridRecommendations = new ArrayList<>();

        List<Recommendation> cfRecs = findHouseCFRecs(prductId, maxItems);
        for (Recommendation cfRec : cfRecs) {
            hybridRecommendations.add(new Recommendation(cfRec.getHid(),cfRec.getCount() * CF_RATING_FACTOR));
        }

        List<Recommendation> cbRecs = findContentBasedMoreLikeRecommendations(prductId,maxItems);
        for (Recommendation cbRec : cbRecs) {
            hybridRecommendations.add(new Recommendation(cbRec.getHid(),cbRec.getCount() * CB_RATING_FACTOR));
        }

        List<Recommendation> streamRecs = findStreamRecs(prductId,maxItems);
        for (Recommendation streamRec : streamRecs) {
            hybridRecommendations.add(new Recommendation(streamRec.getHid(),streamRec.getCount() * SR_RATING_FACTOR));
        }

        Collections.sort(hybridRecommendations, new Comparator<Recommendation>() {
            @Override
            public int compare(Recommendation o1, Recommendation o2) {
               return o1.getCount() > o2.getCount() ? -1:1;
            }
        });
        return hybridRecommendations.subList(0,maxItems > hybridRecommendations.size()? hybridRecommendations.size():maxItems);
    }

    public List<Recommendation> parseRecs(Document document,int maxItems){
        List<Recommendation> recommendations = new ArrayList<>();
        if (document == null || document.isEmpty()){
            return recommendations;
        }
        ArrayList<Document> recs = document.get("recs",ArrayList.class);
        for (Document recDoc : recs) {
          recommendations.add(new Recommendation(recDoc.getInteger("hid"), recDoc.getDouble("count")));
        }
        Collections.sort(recommendations, new Comparator<Recommendation>() {
            @Override
            public int compare(Recommendation o1, Recommendation o2) {
                return o1.getCount() > o2.getCount() ? -1:1;
            }
        });
        return recommendations.subList(0,maxItems > recommendations.size() ? recommendations.size():maxItems);
    }
    public List<Recommendation> parseLongRecs(Document document,int maxItems){
        List<Recommendation> recommendations = new ArrayList<>();
        if (document == null || document.isEmpty()){
            return recommendations;
        }
        ArrayList<Document> recs = document.get("recs",ArrayList.class);
        for (Document recDoc : recs) {
            recommendations.add(new Recommendation(recDoc.getInteger("hid"), recDoc.getDouble("count")));
        }
        Collections.sort(recommendations, new Comparator<Recommendation>() {
            @Override
            public int compare(Recommendation o1, Recommendation o2) {
                return o1.getCount() > o2.getCount() ? -1:1;
            }
        });
        return recommendations.subList(0,maxItems > recommendations.size() ? recommendations.size():maxItems);
    }


    //浏览数量较多的房源推荐
    public List<Recommendation> getHotRecommendation(int maxsize){
        MongoCollection<Document> browseMoreHousesCollection = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_BROWSE_MORE_HOUSE_COLLECTION);
        FindIterable<Document> documents = browseMoreHousesCollection.find().sort(Sorts.descending("sum")).limit(maxsize);
        List<Recommendation> recommendations = new ArrayList<>();
        for (Document document : documents) {
            recommendations.add(new Recommendation(document.getInteger("hid"),0D));
        }
        return recommendations;
    }

    //获取的是按照房源的类型分类浏览排名靠前的推荐
    public List<Recommendation> getTopTypesRecommendations(int maxsize,String genres){
        Document genresTopHouses = mongoClient.getDatabase(Constant.MONGODB_DATABASE).getCollection(Constant.MONGODB_SINGLETYPE_TOP_HOUSE_COLLECTION)
                .find(Filters.eq("genres", genres)).first();
        return parseLongRecs(genresTopHouses,maxsize);
    }

    //ES基于搜索的推荐,暂时用不到 ><
    public List<House2> getContentBasedGenresRecommendation(String genres){
        FuzzyQueryBuilder query = QueryBuilders.fuzzyQuery("singleType", genres);
        Iterable<House2> search = house2SearchService.search(query);
        List<House2> res = new ArrayList<>();
        Iterator<House2> iterator = search.iterator();
        while (iterator.hasNext()){
            res.add(iterator.next());
        }
        return res;
    }
}
