package com.yyb.protocol;

import com.yyb.bean.DupPubRelMessageBO;
import com.yyb.service.DupPubRelMsgService;
import com.yyb.service.DupPublishMsgService;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;

/**
 * Created by yyb on 2022/10/11.
 */
@Slf4j
public class PubRec {

    private final DupPublishMsgService dupPublishMsgService;

    private final DupPubRelMsgService dupPubRelMsgService;

    public PubRec(DupPublishMsgService dupPublishMsgService, DupPubRelMsgService dupPubRelMsgService) {
        this.dupPublishMsgService = dupPublishMsgService;
        this.dupPubRelMsgService = dupPubRelMsgService;
    }

    public void handlePubRec(Channel channel, MqttMessageIdVariableHeader variableHeader) {
        MqttMessage pubRelMessage = MqttMessageFactory.newMessage(
                new MqttFixedHeader(MqttMessageType.PUBREL, false, MqttQoS.AT_MOST_ONCE, false, 0),
                MqttMessageIdVariableHeader.from(variableHeader.messageId()),
                null);
        log.info("PUBREC - clientId: {}, messageId: {}", channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        dupPublishMsgService.remove((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        DupPubRelMessageBO dupPubRelMessageStore = new DupPubRelMessageBO((String) channel.attr(AttributeKey.valueOf("clientId")).get(), variableHeader.messageId());
        dupPubRelMsgService.put((String) channel.attr(AttributeKey.valueOf("clientId")).get(), dupPubRelMessageStore);
        channel.writeAndFlush(pubRelMessage);
    }
}
