package com.yyb.service.impl;

import com.yyb.bean.Message;
import com.yyb.service.ReSendMessageService;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by yyb on 2022/11/1.
 */
@Service
public class ReSendMessageServiceImpl implements ReSendMessageService {

    private Map<String, ConcurrentHashMap<Integer, Message>> reSendMessage = new ConcurrentHashMap<>();

    @Override
    public void put(String clientId, Message message) {
        ConcurrentHashMap<Integer, Message> map = reSendMessage.get(clientId);
        if (map == null) {
            map = new ConcurrentHashMap<>();
            reSendMessage.put(clientId, map);
        }
        map.put(message.getMsgId(), message);
    }

    @Override
    public void remove(String clientId, Integer packetId) {
        if (reSendMessage.containsKey(clientId)) {
            reSendMessage.get(clientId).remove(packetId);
        }
    }

    @Override
    public List<Message> listAllMessage() {
        return null;
    }

    @Override
    public Collection<Message> listMessageByClient(String clientId) {
        ConcurrentHashMap<Integer, Message> result = Optional.ofNullable(reSendMessage.get(clientId)).orElseGet(ConcurrentHashMap::new);
        return result.values();
    }
}
