package p5_ByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import static p5_ByteBuf.LogUtil.log;

public class demo6_slice {
    public static void main(String[] args) {
        ByteBuf buf = ByteBufAllocator.DEFAULT.buffer(10);
        buf.writeBytes(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        log(buf);

        ByteBuf buf1 = buf.slice(0, 5);
        buf1.retain();
        ByteBuf buf2 = buf.slice(5, 5);
        buf2.retain();


        log(buf1);
        log(buf2);
        // 切片得到的ByteBuf不可往里面写入！
        // 切片之前必须把引用计数+1，保证原始buf、各个切片都有一个1，因为切片的底层还是用的是一块内存
        buf.release();
        buf1.release();
        buf2.release();
    }
}
