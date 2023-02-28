package com.yyb.protocol;

import com.yyb.service.DupPublishMsgService;
import com.yyb.service.PacketIdService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPubAckMessage;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by chenws on 2019/10/11.
 */
@Slf4j
public class PubAck {

    private final DupPublishMsgService dupPublishMsgService;

    private final PacketIdService packetIdService;

    public PubAck(DupPublishMsgService dupPublishMsgService, PacketIdService packetIdService) {
        this.dupPublishMsgService = dupPublishMsgService;
        this.packetIdService = packetIdService;
    }

    public void handlePubAck(Channel channel, MqttPubAckMessage msg) {
        int messageId = msg.variableHeader().messageId();
        log.info("PUBACK - clientId: {}, messageId: {}", channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        dupPublishMsgService.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        packetIdService.addPacketId(messageId);
    }
}
