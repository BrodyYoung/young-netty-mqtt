package com.yyb.coder;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.List;

public class ByteBuf2WebSocketEncoder extends MessageToMessageEncoder<ByteBuf> {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        BinaryWebSocketFrame binaryWebSocketFrame = new BinaryWebSocketFrame();
        binaryWebSocketFrame.content().writeBytes(byteBuf);
        list.add(binaryWebSocketFrame);
    }
}
