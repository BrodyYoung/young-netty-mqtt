package com.yyb.session;


import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import lombok.Data;

/**
 * Created by yyb on 2022/10/28.
 */
@Data
public class MqttSession {

    private String clientIdentifier;

    private Channel channel;

    private boolean cleanSession;

    private MqttPublishMessage willMessage;

    public MqttSession(String clientIdentifier, Channel channel, boolean cleanSession, MqttPublishMessage willMessage) {
        this.clientIdentifier = clientIdentifier;
        this.channel = channel;
        this.cleanSession = cleanSession;
        this.willMessage = willMessage;
    }

}
