package com.hjz.test.netty.redisdamboard;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.DefaultEventLoop;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

/**
 * description
 *
 * @author HongJianzhou
 * @date 2025/5/19
 */
public class TestRedisNettyServer {

    public static void main(String[] args) {
        TestRedisNettyServer.run(10086);
    }

    private static void run(int forward) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("boss"));
        EventLoopGroup workerGroup = new NioEventLoopGroup(2, new DefaultThreadFactory("worker"));

        new ServerBootstrap()
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 512)
                .childOption(ChannelOption.AUTO_READ, false)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new RedisForwardHandler("127.0.0.1", 26379));
                    }
                }).bind(forward);
    }

}
