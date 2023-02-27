package com.yyb.session;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenws on 2019/10/10.
 */
@Component
public class MqttSessionCache {

    private final static ConcurrentHashMap<String, MqttSession> sessions = new ConcurrentHashMap<>();

    public Boolean containsKey(String key){
        return sessions.containsKey(key);
    }

    public void put(String key,MqttSession mqttSession){
        sessions.put(key,mqttSession);
    }

    public MqttSession get(String key){
        return sessions.get(key);
    }

    public void remove(String key){
        sessions.remove(key);
    }
}
