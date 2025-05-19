package com.hjz.test.netty.redisdamboard;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.ReferenceCountUtil;

/**
 * description 直接转发redis sentinel报文的handler，可用于应用与redis之间观察相关保报文，或充当F5手动切域名
 *
 * @author HongJianzhou
 * @date 2025/5/19
 */
public class RedisForwardHandler extends ChannelInboundHandlerAdapter {

    private String targetHost;
    private int targetPort;
    private Channel redisChannel;

    public RedisForwardHandler(String targetHost, int targetPort) {
        this.targetHost = targetHost;
        this.targetPort = targetPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // 连接激活时创建客户端用于请求redis sentinel
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(ctx.channel().eventLoop())
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO))
                                .addLast(new TargetHandler(ctx.channel()));
                    }
                }).connect(targetHost, targetPort)
                .addListener((ChannelFutureListener) future -> {
                    if (future.isSuccess()) {
                        redisChannel = future.channel();
                        ctx.channel().config().setAutoRead(true);
                    } else {
                        ctx.close();
                    }
                })
                // TODO 不阻塞可能还没连接上后面请求就进来了，期待更好的解决方案
                .sync();
        ctx.read();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (redisChannel != null && redisChannel.isActive()) {
            redisChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        } else {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (redisChannel != null && redisChannel.isActive()) {
            redisChannel.writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        if (redisChannel != null && redisChannel.isActive()) {
            redisChannel.writeAndFlush(Unpooled.EMPTY_BUFFER)
                    .addListener(ChannelFutureListener.CLOSE);
        }
    }


    private static class TargetHandler extends ChannelInboundHandlerAdapter {

        private final Channel channel;

        public TargetHandler(Channel channel) {
            this.channel = channel;
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            channel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            cause.printStackTrace();
            if (channel != null && channel.isActive()) {
                channel.writeAndFlush(Unpooled.EMPTY_BUFFER)
                        .addListener(ChannelFutureListener.CLOSE);
            }
        }
    }
}
