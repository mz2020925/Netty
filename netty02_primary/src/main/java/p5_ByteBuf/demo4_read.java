package p5_ByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

import static p5_ByteBuf.LogUtil.log;

public class demo4_read {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(new byte[]{1, 2, 3, 4});
        log(buffer);
        buffer.writeCharSequence("hello world", StandardCharsets.UTF_8);
        log(buffer);
        buffer.writeInt(1024);
        log(buffer);

        // 在 read 前先做个标记 mark
        buffer.markReaderIndex();
        byte b1 = buffer.readByte();
        System.out.println(b1);
        // 读指针回到原来的标记那里
        buffer.resetReaderIndex();

        // 每次读取一个字节
        for (int i = 0; i < 4; i++) {
            byte b = buffer.readByte();
            System.out.println(b);
        }
        for (int i = 0; i < 11; i++) {
            byte b = buffer.readByte();
            System.out.println((char) b);
        }
        // 每次读取一个4个字节
        int anInt = buffer.readInt();
        System.out.println(anInt);
    }
}
