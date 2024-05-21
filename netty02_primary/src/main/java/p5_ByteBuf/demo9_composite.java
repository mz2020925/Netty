package p5_ByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.CompositeByteBuf;

import static p5_ByteBuf.LogUtil.log;

public class demo9_composite {
    // 该操作把多个buffer合并到一起，而且是没有拷贝操作的。其实就是把各个buffer的头尾地址拼接到一起。
    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();
        buf1.writeBytes(new byte[]{1, 2, 3, 4});

        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer();
        buf2.writeBytes(new byte[]{6, 7, 8});

        CompositeByteBuf compositeBuffer = ByteBufAllocator.DEFAULT.compositeBuffer();
        compositeBuffer.addComponents(true, buf1, buf2);
        log(compositeBuffer);
    }
}
