package com.yyb.service.impl;

import com.yyb.bean.DupPublishMessageBO;
import com.yyb.constants.RedisConstant;
import com.yyb.service.DupPublishMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

/**
 * Created by yyb on 2022/10/10.
 */
public class DupPublishMsgServiceImpl implements DupPublishMsgService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void put(String clientId, DupPublishMessageBO dupPublishMessageBO) {
        redisTemplate.opsForHash().put(RedisConstant.DUP_PUBLISH_MESSAGE + clientId,dupPublishMessageBO.getMessageId(),dupPublishMessageBO);
    }

    @Override
    public List<DupPublishMessageBO> get(String clientId) {
        Map<Integer,DupPublishMessageBO> entries = redisTemplate.opsForHash().entries(RedisConstant.DUP_PUBLISH_MESSAGE + clientId);
        Map<Integer, DupPublishMessageBO> integerDupPublishMessageBOMap = Optional.ofNullable(entries).orElseGet(this::initMap);
        return new ArrayList<>(integerDupPublishMessageBOMap.values());
    }

    private Map<Integer,DupPublishMessageBO> initMap(){
        return new HashMap<>();
    }

    @Override
    public void remove(String clientId, int messageId) {
        redisTemplate.opsForHash().delete(RedisConstant.DUP_PUBLISH_MESSAGE + clientId,messageId);
    }

    @Override
    public void removeByClient(String clientId) {
        redisTemplate.delete(RedisConstant.DUP_PUBLISH_MESSAGE + clientId);
    }
}
