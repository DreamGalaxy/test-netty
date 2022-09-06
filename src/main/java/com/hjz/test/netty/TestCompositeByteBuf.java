package com.hjz.test.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

/**
 * description
 *
 * @author HongJianzhou
 * @date 2022/7/31
 */
public class TestCompositeByteBuf {

    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer(5);
        buf1.writeBytes(new byte[]{1, 2, 3, 4, 5});

        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer(5);
        buf2.writeBytes(new byte[]{6, 7, 8, 9, 10});

        // 不推荐写法：新申请一块内存并分别写入
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(10);
        buffer.writeBytes(buf1).writeBytes(buf2);

        // 将多个byteBuf组合，避免了内存的符合但维护会更加复杂
        CompositeByteBuf compositeByteBuf = ByteBufAllocator.DEFAULT.compositeBuffer();
        // 第一个boolean的变量不能丢，否则写指针不会增长不会真正把几个buf写进去
        compositeByteBuf.addComponents(true, buf1, buf2);
    }

}
