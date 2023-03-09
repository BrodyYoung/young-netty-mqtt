package com.yyb.bean;

import com.yyb.topic.Topic;
import lombok.NoArgsConstructor;

import java.util.Objects;

/**
 * Created by yyb on 2022/10/9.
 */
@NoArgsConstructor
public class SubscribeBO {

    private String clientId;

    private Topic topicFilter;

    private int mqttQoS;

    public SubscribeBO(String clientId, Topic topicFilter, int mqttQoS) {
        this.clientId = clientId;
        this.topicFilter = topicFilter;
        this.mqttQoS = mqttQoS;
    }

    public SubscribeBO(SubscribeBO subscribeBO) {
        this.mqttQoS = subscribeBO.mqttQoS;
        this.clientId = subscribeBO.clientId;
        this.topicFilter = subscribeBO.topicFilter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SubscribeBO that = (SubscribeBO) o;

        if (!Objects.equals(clientId, that.clientId))
            return false;
        return Objects.equals(topicFilter, that.topicFilter);
    }

    @Override
    public int hashCode() {
        int result = clientId != null ? clientId.hashCode() : 0;
        result = 31 * result + (topicFilter != null ? topicFilter.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("[filter:%s, clientID: %s, qos: %s]", topicFilter, clientId, mqttQoS);
    }

    @Override
    public SubscribeBO clone() {
        try {
            return (SubscribeBO) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Topic getTopicFilter() {
        return topicFilter;
    }

    public void setTopicFilter(Topic topicFilter) {
        this.topicFilter = topicFilter;
    }

    public int getMqttQoS() {
        return mqttQoS;
    }

    public void setMqttQoS(int mqttQoS) {
        this.mqttQoS = mqttQoS;
    }
}
