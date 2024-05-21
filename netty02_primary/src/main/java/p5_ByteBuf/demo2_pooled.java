package p5_ByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class demo2_pooled {
    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer();  // 默认是池化的直接内存buffer
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.heapBuffer();
        ByteBuf buf3 = ByteBufAllocator.DEFAULT.directBuffer();

        System.out.println(buf1.getClass());
        System.out.println(buf2.getClass());
        System.out.println(buf3.getClass());

        // log(buf1);
        // StringBuilder sb = new StringBuilder();
        // for (int i = 0; i < 300; i++) {
        //     sb.append("a");
        // }
        // buf1.writeBytes(sb.toString().getBytes());
        // log(buf1);

    }
}
