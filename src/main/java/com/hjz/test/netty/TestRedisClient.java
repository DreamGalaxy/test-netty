package com.hjz.test.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * description
 *
 * @author HongJianzhou
 * @date 2022/7/31
 */
@Slf4j
public class TestRedisClient {

    public static void main(String[] args) {
        NioEventLoopGroup worker = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(worker)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new ChannelInboundHandlerAdapter() {
                                        @Override
                                        public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                            ByteBuf buf = ctx.alloc().buffer();
                                            // 数组长度
                                            buf.writeBytes("*3".getBytes());
                                            buf.writeBytes("\r\n".getBytes());
                                            // 字符串长度
                                            buf.writeBytes("$3".getBytes());
                                            buf.writeBytes("\r\n".getBytes());
                                            // 字符串
                                            buf.writeBytes("set".getBytes());
                                            buf.writeBytes("\r\n".getBytes());

                                            buf.writeBytes("$4".getBytes());
                                            buf.writeBytes("\r\n".getBytes());
                                            buf.writeBytes("name".getBytes());
                                            buf.writeBytes("\r\n".getBytes());

                                            buf.writeBytes("$8".getBytes());
                                            buf.writeBytes("\r\n".getBytes());
                                            buf.writeBytes("zhangsan".getBytes());
                                            buf.writeBytes("\r\n".getBytes());
                                            ctx.writeAndFlush(buf);
                                        }

                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            ByteBuf buf = (ByteBuf) msg;
                                            log.info("redis返回内容：{}", buf.toString(Charset.defaultCharset()));
                                        }
                                    });
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 6379).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("client error", e);
        } finally {
            worker.shutdownGracefully();
        }
    }

}
