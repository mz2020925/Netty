package p5_ByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;

import static p5_ByteBuf.LogUtil.log;

public class demo10_Unpooled {
    // 前面我们讲的都是池化的ByteBuf的slice、composite等等操作，对于非池化的ByteBuf的操作需要通过Unpooled这个工具类来实现
    public static void main(String[] args) {
        ByteBuf buf1 = ByteBufAllocator.DEFAULT.buffer(5);
        buf1.writeBytes(new byte[]{1, 2, 3, 4, 5, 6});
        ByteBuf buf2 = ByteBufAllocator.DEFAULT.buffer(5);
        buf2.writeBytes(new byte[]{6, 7, 8, 9, 10});

        // 当包装 ByteBuf 个数超过一个时, 底层使用了 CompositeByteBuf
        ByteBuf buf3 = Unpooled.wrappedBuffer(buf1, buf2);
        // System.out.println(ByteBufUtil.prettyHexDump(buf3));
        log(buf3);

        // 还可以不用创建buf1和buf2那么麻烦，直接合并两个数组即可
        ByteBuf buf4 = Unpooled.wrappedBuffer(new byte[]{1, 2, 3}, new byte[]{4, 5, 6});
        // System.out.println(buf4.getClass());
        // System.out.println(ByteBufUtil.prettyHexDump(buf4));
        log(buf4);
    }

}
