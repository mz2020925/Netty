package p5_ByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.nio.charset.StandardCharsets;

import static p5_ByteBuf.LogUtil.log;

public class demo3_write {
        public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer();
        buffer.writeBytes(new byte[]{1,2,3,4});
        log(buffer);
        buffer.writeCharSequence("hello world", StandardCharsets.UTF_8);
        log(buffer);
        buffer.writeInt(1024);
        log(buffer);
    }
}
