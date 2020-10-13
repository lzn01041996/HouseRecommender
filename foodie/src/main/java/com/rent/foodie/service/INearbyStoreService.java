package com.rent.foodie.service;

import com.rent.foodie.es.pojo.GEO;

/*
@李子宁
Happy,happy everyday!
冲鸭！
2020/8/21

*/
public interface INearbyStoreService {
    void nearbyStoreList(GEO locationModel, Integer page, Integer size);

}
