package com.yyb.handler;

import com.yyb.constants.MqttTypeConstant;
import com.yyb.protocol.MqttProcess;
import com.yyb.session.MqttSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;

/**
 * Created by yyb on 2022/8/31.
 */
@ChannelHandler.Sharable
@Slf4j
public class MqttTransportHandler extends ChannelInboundHandlerAdapter implements GenericFutureListener<Future<? super Void>> {

    private MqttProcess process;

//    MqttTransportHandler(MqttProcess process) {
//        this.process = process;
//    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MqttMessage mqttMessage = (MqttMessage) msg;
        log.info("Accept msg: {}", mqttMessage);
        handleMqttMessage(ctx, mqttMessage);
    }

    private void handleMqttMessage(ChannelHandlerContext ctx, MqttMessage msg) {
        InetSocketAddress address = (InetSocketAddress) ctx.channel().remoteAddress();
        if (msg.fixedHeader() == null) {
            log.info("[{}:{}] Invalid message received", address.getHostName(), address.getPort());
            process.getDisConnect().handleDisConnect(ctx.channel(), msg);
            return;
        }
        switch (msg.fixedHeader().messageType()) {
            case CONNECT:
                Runnable connectTask = () -> process.getConnect().handleConnect(ctx.channel(), (MqttConnectMessage) msg);
                process.getExecutorManager().getExecutor(MqttTypeConstant.CONNECT).submit(connectTask);
                break;
            case PUBLISH:
                Runnable publishTask = () -> process.getPublish().handlePublish(ctx.channel(), (MqttPublishMessage) msg);
                process.getExecutorManager().getExecutor(MqttTypeConstant.PUBLISH).submit(publishTask);
                break;
            case PUBACK:
                Runnable pubAckTask = () -> process.getPubAck().handlePubAck(ctx.channel(), (MqttPubAckMessage) msg);
                process.getExecutorManager().getExecutor(MqttTypeConstant.PUB_ACK).submit(pubAckTask);
                break;
            case PUBREC:
                Runnable pubRecTask = () -> process.getPubRec().handlePubRec(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                process.getExecutorManager().getExecutor(MqttTypeConstant.PUB_REC).submit(pubRecTask);
                break;
            case PUBREL:
                Runnable pubRelTask = () -> process.getPubRel().handlePubRel(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                process.getExecutorManager().getExecutor(MqttTypeConstant.PUB_REL).submit(pubRelTask);
                break;
            case PUBCOMP:
                Runnable pubCompTask = () -> process.getPubComp().handlePubComp(ctx.channel(), (MqttMessageIdVariableHeader) msg.variableHeader());
                process.getExecutorManager().getExecutor(MqttTypeConstant.PUB_COMP).submit(pubCompTask);
                break;
            case SUBSCRIBE:
                Runnable subscribeTask = () -> process.getSubscribe().handleSubscribe(ctx.channel(), (MqttSubscribeMessage) msg);
                process.getExecutorManager().getExecutor(MqttTypeConstant.SUBSCRIBE).submit(subscribeTask);
                break;
            case UNSUBSCRIBE:
                Runnable unSubscribeTask = () -> process.getUnSubscribe().handleUnSubscribe(ctx.channel(), (MqttUnsubscribeMessage) msg);
                process.getExecutorManager().getExecutor(MqttTypeConstant.UNSUBSCRIBE).submit(unSubscribeTask);
                break;
            case PINGREQ:
                Runnable pingReqTask = () -> process.getPingReq().handlePingReq(ctx.channel(), msg);
                process.getExecutorManager().getExecutor(MqttTypeConstant.PING_REQ).submit(pingReqTask);
                break;
            case DISCONNECT:
                Runnable disconnectTask = () -> process.getDisConnect().handleDisConnect(ctx.channel(), msg);
                process.getExecutorManager().getExecutor(MqttTypeConstant.DISCONNECT).submit(disconnectTask);
                break;
            default:
                break;
        }
    }

    @Override
    public void operationComplete(Future<? super Void> future) throws Exception {
        if (future.isSuccess()){
            log.info("future success");
        }else{
            log.info("future fail");
        }
    }

    /**
     * 长时间没接受或发送
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            if (idleStateEvent.state() == IdleState.ALL_IDLE) {
                Channel channel = ctx.channel();
                String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
                // 发送遗嘱消息
                if (process.getMqttSessionCache().containsKey(clientId)) {
                    MqttSession mqttSession = process.getMqttSessionCache().get(clientId);
                    if (mqttSession.getWillMessage() != null) {
                        Runnable publishTask = () -> process.getPublish().handlePublish(ctx.channel(), mqttSession.getWillMessage());
                        process.getExecutorManager().getExecutor(MqttTypeConstant.PUBLISH).submit(publishTask);
                    }
                }
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    /**
     * 中断连接
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(cause instanceof IOException){
            Channel channel = ctx.channel();
            String clientId = (String) channel.attr(AttributeKey.valueOf("clientId")).get();
            // 发送遗嘱消息
            if (process.getMqttSessionCache().containsKey(clientId)) {
                MqttSession mqttSession = process.getMqttSessionCache().get(clientId);
                if (mqttSession.getWillMessage() != null) {
                    process.getPublish().handlePublish(ctx.channel(), mqttSession.getWillMessage());
                }
            }
            ctx.close();
        }else {
            super.exceptionCaught(ctx, cause);
        }
    }
}
