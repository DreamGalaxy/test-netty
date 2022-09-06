package com.hjz.test.netty;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * description
 *
 * @author HongJianzhou
 * @date 2022/4/28
 */
@Slf4j
public class TestEventLoopGroup {

    public static void main(String[] args) {
        // 未指定线程数时默认为cpu*2
        EventLoopGroup group = new NioEventLoopGroup(2);

        // 获取下一个事件循环对象
        log.info("{}", group.next());
        log.info("{}", group.next());
        log.info("{}", group.next());
        log.info("{}", group.next());

        // 执行普通任务
        group.next().execute(() -> log.info("ok"));
        group.next().submit(() -> log.info("ok"));

        // 执行定时任务，和ScheduleExecutorService一样
        group.next().scheduleAtFixedRate(() -> log.info("定时任务"), 0, 1, TimeUnit.SECONDS);
    }

}
