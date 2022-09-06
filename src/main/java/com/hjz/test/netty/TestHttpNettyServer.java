package com.hjz.test.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * description
 *
 * @author HongJianzhou
 * @date 2022/7/31
 */
@Slf4j
public class TestHttpNettyServer {

    public static void main(String[] args) {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(4);
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap().group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) throws Exception {
                            channel.pipeline().addLast(new IdleStateHandler(5, 0, 0))
                                    .addLast(new ChannelDuplexHandler() {
                                        @Override
                                        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
                                            super.userEventTriggered(ctx, evt);
                                            // 用来触发特殊事件
                                            if (evt instanceof IdleStateEvent) {
                                                IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
                                                if (idleStateEvent == IdleStateEvent.READER_IDLE_STATE_EVENT) {
                                                    log.info("读空闲");
                                                }
                                            }
                                        }
                                    })
                                    .addLast(new LoggingHandler(LogLevel.DEBUG))
                                    .addLast(new HttpServerCodec())
                                    .addLast(new HttpObjectAggregator(5 * 1024 * 1024))
                                    .addLast(new SimpleChannelInboundHandler<FullHttpRequest>() {
                                        @Override
                                        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
                                            log.info("请求行：{}\n", request.uri());
                                            log.info("请求头：{}\n", request);
                                            log.info("请求体：{}\n", request.content().toString(StandardCharsets.UTF_8));

                                            // 返回响应，协议与请求的版本一致
                                            DefaultFullHttpResponse response =
                                                    new DefaultFullHttpResponse(request.protocolVersion(), HttpResponseStatus.OK);
                                            byte[] bytes = "success".getBytes();
                                            // 设置Content-Length避免客户端不知道什么时候结束
                                            response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, bytes.length);
                                            response.content().writeBytes(bytes);
                                            // 写回响应
                                            ctx.writeAndFlush(response);
                                            // 如果要改为短连接，则直接关闭
                                            //ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
                                        }
                                    });
                        }
                    });
            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            log.error("server error", e);
        } finally {
            boss.shutdownGracefully();
            worker.shutdownGracefully();
        }
    }

}
