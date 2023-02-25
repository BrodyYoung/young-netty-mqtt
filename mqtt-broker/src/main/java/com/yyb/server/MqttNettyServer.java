package com.yyb.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;

/**
 * Created by BrodyYoung on 2022/10/22.
 */
public class MqttNettyServer {

    public static void main(String[] args) {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast("decoder", new MqttDecoder(65536));
                            pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                        }
                    })
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //设置发送的缓冲大小
                    .childOption(ChannelOption.SO_SNDBUF, 65536)
                    //设置接收的缓冲大小
                    .option(ChannelOption.SO_RCVBUF, 65536)
                    .option(ChannelOption.SO_BACKLOG, 1024);
            int port = 8883;
            ChannelFuture channelFuture = b.bind(port).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
