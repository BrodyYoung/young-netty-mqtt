package com.yyb.protocol;

import cn.hutool.core.util.StrUtil;
import com.yyb.bean.DupPublishMessageBO;
import com.yyb.bean.RetainMessageBO;
import com.yyb.bean.SubscribeBO;
import com.yyb.service.DupPublishMsgService;
import com.yyb.service.PacketIdService;
import com.yyb.service.RetainMsgService;
import com.yyb.service.SubscribeService;
import com.yyb.topic.Topic;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by chenws on 2019/10/11.
 */
@Slf4j
public class Subscribe {

    private final SubscribeService subscribeService;

    private final RetainMsgService retainMsgService;

    private final PacketIdService packetIdService;

    private final DupPublishMsgService dupPublishMsgService;

    public Subscribe(SubscribeService subscribeService, RetainMsgService retainMsgService, PacketIdService packetIdService, DupPublishMsgService dupPublishMsgService) {
        this.subscribeService = subscribeService;
        this.retainMsgService = retainMsgService;
        this.packetIdService = packetIdService;
        this.dupPublishMsgService = dupPublishMsgService;
    }


    public void handleSubscribe(Channel channel, MqttSubscribeMessage msg) {
        List<MqttTopicSubscription> topicSubscriptions = msg.payload().topicSubscriptions();
        if (this.validTopicFilter(topicSubscriptions)) {
            String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
            List<Integer> mqttQoSList = new ArrayList<>();
            for(MqttTopicSubscription mqttTopicSubscription : topicSubscriptions){
                String topicFilter = mqttTopicSubscription.topicName();
                MqttQoS mqttQoS = mqttTopicSubscription.qualityOfService();
                this.sendRetainMessage(channel, topicFilter, mqttQoS);
                Topic topic = new Topic(topicFilter);
                SubscribeBO subscribeBO = new SubscribeBO(clientId, topic, mqttQoS.value());
                subscribeService.put(topicFilter, subscribeBO);
                mqttQoSList.add(mqttQoS.value());
                log.info("SUBSCRIBE - clientId: {}, topFilter: {}, QoS: {}", clientId, topicFilter, mqttQoS.value());
            }
            MqttSubAckMessage subAckMessage = (MqttSubAckMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.SUBACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                    MqttMessageIdVariableHeader.from(msg.variableHeader().messageId()),
                    new MqttSubAckPayload(mqttQoSList));
            channel.writeAndFlush(subAckMessage);
        } else {
            log.info("订阅主题中包含非法主题：{}", topicSubscriptions);
            channel.close();
        }
    }

    private boolean validTopicFilter(List<MqttTopicSubscription> topicSubscriptions) {
        for (MqttTopicSubscription topicSubscription : topicSubscriptions) {
            String topicFilter = topicSubscription.topicName();
            // 以#或+符号开头的、以/符号结尾的订阅按非法订阅处理, 这里没有参考标准协议
            if (StrUtil.startWith(topicFilter, '+') || StrUtil.endWith(topicFilter, '/')) {
                return false;
            }
            if (StrUtil.contains(topicFilter, '#')) {
                // 如果出现多个#符号的订阅按非法订阅处理
                if (StrUtil.count(topicFilter, '#') > 1) {
                    return false;
                }
            }
            if (StrUtil.contains(topicFilter, '+')) {
                //如果+符号和/+字符串出现的次数不等的情况按非法订阅处理
                if (StrUtil.count(topicFilter, '+') != StrUtil.count(topicFilter, "/+")) {
                    return false;
                }

            }

        }
        return true;
    }

    private void sendRetainMessage(Channel channel, String topicFilter, MqttQoS mqttQoS) {
        List<RetainMessageBO> retainMessageStores = retainMsgService.search(topicFilter);
        String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
        retainMessageStores.forEach(retainMessageStore -> {
            MqttQoS respQoS = retainMessageStore.getMqttQoS() > mqttQoS.value() ? mqttQoS : MqttQoS.valueOf(retainMessageStore.getMqttQoS());
            if (respQoS == MqttQoS.AT_MOST_ONCE) {
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessageStore.getTopic(), 0), Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));
                log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}", clientId, retainMessageStore.getTopic(), respQoS.value());
                channel.writeAndFlush(publishMessage);
            }
            if (respQoS == MqttQoS.AT_LEAST_ONCE || respQoS == MqttQoS.EXACTLY_ONCE) {
                Integer packetId = packetIdService.getPacketId();
                MqttPublishMessage publishMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH, false, respQoS, false, 0),
                        new MqttPublishVariableHeader(retainMessageStore.getTopic(), packetId), Unpooled.buffer().writeBytes(retainMessageStore.getMessageBytes()));
                log.info("PUBLISH - clientId: {}, topic: {}, Qos: {}, messageId: {}", clientId, retainMessageStore.getTopic(), respQoS.value(), 1);
                channel.writeAndFlush(publishMessage);
                DupPublishMessageBO dupPublishMessageBO = new DupPublishMessageBO(clientId,retainMessageStore.getTopic(),respQoS.value(),packetId,retainMessageStore.getMessageBytes());
                dupPublishMsgService.put(clientId,dupPublishMessageBO);
            }
        });
    }
}
