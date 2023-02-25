package com.yyb.service.impl;

import com.yyb.bean.DupPubRelMessageBO;
import com.yyb.constants.RedisConstant;
import com.yyb.service.DupPubRelMsgService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by chenws on 2019/10/10.
 */
@Service
public class DupPubRelMsgServiceImpl implements DupPubRelMsgService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void put(String clientId, DupPubRelMessageBO dupPubRelMessageStore) {
        redisTemplate.opsForHash().put(RedisConstant.DUP_PUBREL_MESSAGE + clientId,dupPubRelMessageStore.getMessageId(),dupPubRelMessageStore);
    }

    @Override
    public List<DupPubRelMessageBO> get(String clientId) {
        Map<Integer, DupPubRelMessageBO> entries = redisTemplate.opsForHash().entries(RedisConstant.DUP_PUBREL_MESSAGE + clientId);
        Map<Integer, DupPubRelMessageBO> integerDupPublishMessageBOMap = Optional.ofNullable(entries).orElseGet(this::initMap);
        return new ArrayList<>(integerDupPublishMessageBOMap.values());
    }

    private Map<Integer,DupPubRelMessageBO> initMap(){
        return new HashMap<>();
    }

    @Override
    public void remove(String clientId, int messageId) {
        redisTemplate.opsForHash().delete(RedisConstant.DUP_PUBREL_MESSAGE + clientId,messageId);
    }

    @Override
    public void removeByClient(String clientId) {
        redisTemplate.delete(RedisConstant.DUP_PUBREL_MESSAGE + clientId);
    }
}
