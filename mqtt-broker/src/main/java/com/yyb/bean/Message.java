package com.yyb.bean;

import io.netty.handler.codec.mqtt.MqttMessageType;
import lombok.Data;
import lombok.ToString;

import java.io.Serializable;
import java.util.Map;

@Data
@ToString
public class Message implements Serializable {

    private int msgId;

    private int qos;

    private String topic;

    private Map<String,Object> headers;

    private String clientId;

    private MqttMessageType type;

    private byte[] payload;

}
