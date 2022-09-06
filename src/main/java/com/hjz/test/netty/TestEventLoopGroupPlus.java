package com.hjz.test.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * description
 *
 * @author HongJianzhou
 * @date 2022/4/29
 */
@Slf4j
public class TestEventLoopGroupPlus {

    public static void main(String[] args) {
        // 用于处理耗时业务的独立EventLoopGroup
        EventLoopGroup business = new DefaultEventLoop();
        new ServerBootstrap()
                // 传递两个NioEventLoopGroup，第一个为boss，第二个为worker
                .group(new NioEventLoopGroup(), new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast("handler1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.info("{}", buf.toString(StandardCharsets.UTF_8));
                                // 把消息传递给下一个handler
                                ctx.fireChannelRead(msg);
                            }
                        });
                        // 指定处理该handler的eventLoop为自定义的business，避免因为业务耗时过长导致worker不能及时处理其他channel的任务
                        ch.pipeline().addLast(business, "handler2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.info("{}", buf.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                })
                .bind(8080);
    }

}
