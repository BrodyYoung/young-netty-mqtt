package com.yyb.protocol;

import com.yyb.service.DupPubRelMsgService;
import com.yyb.service.PacketIdService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessageIdVariableHeader;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by yyb on 2022/10/11.
 */
@Slf4j
public class PubComp {

    private final DupPubRelMsgService dupPubRelMsgService;

    private final PacketIdService packetIdService;

    public PubComp(DupPubRelMsgService dupPubRelMsgService, PacketIdService packetIdService) {
        this.dupPubRelMsgService = dupPubRelMsgService;
        this.packetIdService = packetIdService;
    }

    public void handlePubComp(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        int messageId = variableHeader.messageId();
        log.info("PUBCOMP - clientId: {}, messageId: {}", channel.attr(AttributeKey.valueOf("clientId")).get(), messageId);
        dupPubRelMsgService.remove((String)channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        packetIdService.addPacketId(messageId);
    }
}
