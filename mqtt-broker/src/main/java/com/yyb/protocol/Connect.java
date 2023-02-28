package com.yyb.protocol;

import com.yyb.bean.DupPubRelMessageBO;
import com.yyb.bean.DupPublishMessageBO;
import com.yyb.service.DupPubRelMsgService;
import com.yyb.service.DupPublishMsgService;
import com.yyb.service.IdentifyValidService;
import com.yyb.service.SubscribeService;
import com.yyb.session.MqttSession;
import com.yyb.session.MqttSessionCache;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;

/**
 * Created by chenws on 2019/10/9.
 */
public class Connect {

    private final MqttSessionCache mqttSessionCache;

    private final SubscribeService subscribeService;

    private final DupPublishMsgService dupPublishMsgService;

    private final DupPubRelMsgService dupPubRelMsgService;

    private final IdentifyValidService identifyValidService;

    public Connect(MqttSessionCache mqttSessionCache, SubscribeService subscribeService, DupPublishMsgService dupPublishMsgService, DupPubRelMsgService dupPubRelMsgService, @Qualifier("password") IdentifyValidService identifyValidService) {
        this.mqttSessionCache = mqttSessionCache;
        this.subscribeService = subscribeService;
        this.dupPublishMsgService = dupPublishMsgService;
        this.dupPubRelMsgService = dupPubRelMsgService;
        this.identifyValidService = identifyValidService;
    }

    public void handleConnect(Channel channel, MqttConnectMessage msg) {
        if (msg.decoderResult().isFailure()) {
            Throwable cause = msg.decoderResult().cause();
            if (cause instanceof MqttUnacceptableProtocolVersionException) {
                replyConnAckMessage(channel, MqttConnectReturnCode.CONNECTION_REFUSED_UNACCEPTABLE_PROTOCOL_VERSION);
                return;
            } else if (cause instanceof MqttIdentifierRejectedException) {
                replyConnAckMessage(channel,MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
                return;
            }
            channel.close();
            return;
        }
        String clientIdentifier = msg.payload().clientIdentifier();
        if (StringUtils.isBlank(clientIdentifier)) {
            replyConnAckMessage(channel,MqttConnectReturnCode.CONNECTION_REFUSED_IDENTIFIER_REJECTED);
            return;
        }
        //验证客户端
        String userName = msg.payload().userName();
        byte[] password = msg.payload().passwordInBytes();
        identifyValidService.isValid(userName,password,clientIdentifier);

        if(mqttSessionCache.containsKey(clientIdentifier)){
            MqttSession mqttSession = mqttSessionCache.get(clientIdentifier);
            mqttSession.getChannel().close();
            mqttSessionCache.remove(clientIdentifier);
        }

        boolean cleanSession = msg.variableHeader().isCleanSession();
        if(cleanSession){
            //清除相关的主题订阅
            subscribeService.removeByClient(clientIdentifier);
            //清除重发publish消息
            dupPublishMsgService.removeByClient(clientIdentifier);
            //清除重发pubrel消息
            dupPubRelMsgService.removeByClient(clientIdentifier);
        }

        MqttSession mqttSession = new MqttSession(msg.payload().clientIdentifier(), channel, msg.variableHeader().isCleanSession(), null);
        //处理遗嘱信息
        if (msg.variableHeader().isWillFlag()){
            MqttPublishMessage willMessage = (MqttPublishMessage) MqttMessageFactory.newMessage(
                    new MqttFixedHeader(MqttMessageType.PUBLISH,false, MqttQoS.valueOf(msg.variableHeader().willQos()),msg.variableHeader().isWillRetain(),0),
                    new MqttPublishVariableHeader(msg.payload().willTopic(),0),
                    Unpooled.buffer().writeBytes(msg.payload().willMessageInBytes())
            );
            mqttSession.setWillMessage(willMessage);
        }
        //处理连接心跳包
        if (msg.variableHeader().keepAliveTimeSeconds() > 0){
            channel.pipeline().addFirst("idle",new IdleStateHandler(0, 0, Math.round(msg.variableHeader().keepAliveTimeSeconds() * 1.5f)));
        }
        mqttSessionCache.put(msg.payload().clientIdentifier(),mqttSession);
        channel.attr(AttributeKey.valueOf("clientId")).set(msg.payload().clientIdentifier());
        MqttConnAckMessage mqttConnAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK,false,MqttQoS.AT_MOST_ONCE,false,0),
                new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED,!cleanSession),
                null
        );
        channel.writeAndFlush(mqttConnAckMessage);

        // 如果cleanSession为0, 需要重发同一clientId存储的未完成的QoS1和QoS2的DUP消息
        if (!cleanSession){
            List<DupPublishMessageBO> dupPublishMessageBOS = dupPublishMsgService.get(msg.payload().clientIdentifier());
            List<DupPubRelMessageBO> dupPubRelMessageBOS = dupPubRelMsgService.get(msg.payload().clientIdentifier());
            dupPublishMessageBOS.forEach(dupPublishMessageBO -> {
                MqttPublishMessage publishMessage = (MqttPublishMessage)MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBLISH,false,MqttQoS.valueOf(dupPublishMessageBO.getMqttQoS()),false,0),
                        new MqttPublishVariableHeader(dupPublishMessageBO.getTopicFilter(),dupPublishMessageBO.getMessageId()),
                        Unpooled.buffer().writeBytes(dupPublishMessageBO.getMessageBytes())
                );
                channel.writeAndFlush(publishMessage);
            });
            dupPubRelMessageBOS.forEach(dupPubRelMessageBO -> {
                MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                        new MqttFixedHeader(MqttMessageType.PUBREL,false,MqttQoS.AT_MOST_ONCE,false,0),
                        MqttMessageIdVariableHeader.from(dupPubRelMessageBO.getMessageId()),
                        null
                );
                channel.writeAndFlush(pubRelMessage);
            });
        }
    }

    private void replyConnAckMessage(Channel channel,MqttConnectReturnCode mqttConnectReturnCode){
        MqttConnAckMessage connAckMessage = (MqttConnAckMessage) MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0),
                new MqttConnAckVariableHeader(mqttConnectReturnCode, false), null);
        channel.writeAndFlush(connAckMessage);
        channel.close();
    }
}
