package com.yyb.service.impl;

import com.yyb.bean.SubscribeBO;
import com.yyb.service.SubscribeService;
import com.yyb.subscribe.SubscribeClientCache;
import com.yyb.topic.CTrie;
import com.yyb.topic.Topic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by chenws on 2019/10/10.
 */
@Service
public class SubscribeServiceImpl implements SubscribeService {

    @Autowired
    private SubscribeClientCache subscribeClientCache;

    @Autowired
    private CTrie cTrie;

    @Override
    public void put(String topicFilter, SubscribeBO subscribeBO) {
        cTrie.addToTree(subscribeBO);
        subscribeClientCache.putTopicFilter(subscribeBO.getClientId(),topicFilter);
    }

    @Override
    public void remove(String topicFilter, String clientId) {
        Topic topic = new Topic(topicFilter);
        cTrie.removeFromTree(topic,clientId);
        subscribeClientCache.removeTopicFilter(clientId,topicFilter);
    }

    @Override
    public void removeByClient(String clientId) {
        Set<String> topicFilters = subscribeClientCache.topicFilterByClientId(clientId);
        topicFilters.forEach(topicFilter -> {
            Topic topic = new Topic(topicFilter);
            cTrie.removeFromTree(topic,clientId);
        });
    }

    @Override
    public Set<SubscribeBO> search(String topicFilter) {
        Topic topic = new Topic(topicFilter);
        if(topic.isValid()){
            return cTrie.recursiveMatch(topic);
        }else{
            return new HashSet<>();
        }

    }

}
