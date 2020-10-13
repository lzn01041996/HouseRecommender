package com.rent.foodie.mongo.pojo;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/25

*/
public class Constant {
    public static String MONGODB_DATABASE = "houserec";

    public static String MONGODB_USER_COLLECTION= "User";

    public static String MONGODB_HOUSE_COLLECTION = "House";

    public static String MONGODB_BROWSE_COLLECTION = "Browse";

    public static String MONGODB_BROWSES_COLLECTION = "Browses";

    public static String MONGODB_AVERAGE_BROWSE_COLLECTION = "AverageBrowse";

    public static String MONGODB_HOUSE_RECS_COLLECTION = "HouseRecs";

    public static String MONGODB_USER_RECS_COLLECTION = "UserRecs";

    public static String MONGODB_STREAM_RECS_COLLECTION = "StreamRecs";

    public static String MONGODB_CONTENT_HOUSE_RECS_COLLECTION = "ContentHouseRecs";

    public static  String MONGODB_BROWSE_MORE_HOUSE_COLLECTION = "AverageBrowse";

    public static String MONGODB_SINGLETYPE_TOP_HOUSE_COLLECTION = "SingleTypeTopHouses";


    //************** FOR ELEASTICSEARCH ****************

    public static String ES_INDEX = "house";

    public static String ES_HOSUE_TYPE = "house";


    //************** FOR MOVIE RATING ******************

    public static String HOUSE_BROWSE_PREFIX = "HOUSE_BROWSE_PREFIX";

    public static int REDIS_HOUSE_RATING_QUEUE_SIZE = 40;



}
