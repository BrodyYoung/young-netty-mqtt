package com.yyb.protocol;

import com.yyb.session.MqttSession;
import com.yyb.session.MqttSessionCache;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by chenws on 2019/10/10.
 */
@Slf4j
public class DisConnect {

    private final MqttSessionCache mqttSessionCache;

    public DisConnect(MqttSessionCache mqttSessionCache) {
        this.mqttSessionCache = mqttSessionCache;
    }

    public void handleDisConnect(Channel channel, MqttMessage msg) {
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        MqttSession mqttSession = mqttSessionCache.get(clientId);
        if (mqttSession != null) {
            //清除遗嘱消息
            mqttSession.setWillMessage(null);
        }
        log.info("DISCONNECT - clientId: {}", clientId);
        //关闭连接
        channel.close();
    }
}
