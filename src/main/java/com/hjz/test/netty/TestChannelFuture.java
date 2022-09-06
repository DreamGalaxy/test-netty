package com.hjz.test.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

/**
 * description
 *
 * @author HongJianzhou
 * @date 2022/4/29
 */
public class TestChannelFuture {

    public static void main(String[] args) throws InterruptedException {
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new StringEncoder());
                    }
                })
                // 1.连接到服务器
                // 异步非阻塞，main发起了调用，真正执行connect的是nio线程
                .connect(new InetSocketAddress("localhost", 8080));
        // 方法一：2.使用sync方法同步等待处理结果，阻塞住当前线程，直到nio线程连接建立完毕
//        channelFuture.sync();
//        // 获取客服端-服务器间的channel对象
//        Channel channel = channelFuture.channel();
//        channel.writeAndFlush("hello world");

        // 方法二：使用addListener 方法异步处理结果
        channelFuture.addListener(new ChannelFutureListener() {
            // 在nio线程连接建立完成后，会调用operationComplete方法
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                Channel channel = future.channel();
                channel.writeAndFlush("hello world");
            }
        });
    }

}
