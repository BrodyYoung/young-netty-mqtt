package com.yyb.service;


import com.yyb.bean.SubscribeBO;

import java.util.Set;

/**
 * Created by chenws on 2019/10/9.
 */
public interface SubscribeService {

    /**
     * 保存订阅
     * @param topicFilter
     * @param subscribeBO
     */
    void put(String topicFilter, SubscribeBO subscribeBO);

    /**
     * 删除订阅
     * @param topicFilter
     * @param clientId
     */
    void remove(String topicFilter, String clientId);

    /**
     * 删除clientId的订阅
     * @param clientId
     */
    void removeByClient(String clientId);

    /**
     * 获取订阅存储集
     * @param topic
     * @return
     */
    Set<SubscribeBO> search(String topic);

}
