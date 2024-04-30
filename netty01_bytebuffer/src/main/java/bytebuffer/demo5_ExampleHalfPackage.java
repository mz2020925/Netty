package bytebuffer;

import org.junit.Test;

import java.nio.ByteBuffer;

import static bytebuffer.ByteBufferUtil.debugAll;

/*
网络上有多条数据发送给服务端，数据之间使用 \n 进行分隔
但由于某种原因这些数据在接收时，被进行了重新组合，例如原始数据有3条为

Hello,world\n
I'm zhangsan\n
How are you?\n

变成了下面的两个 byteBuffer (黏包，半包)

Hello,world\nI'm zhangsan\nHo
w are you?\n

请恢复原始消息。

黏包原因：客户端给服务端发数据的时候，为了效率更高，会把 Hello,world\n 和 I'm zhangsan\n 打包一起发送。
半包原因：服务端的缓存区的大小是一定的，如果客户端发来的 打包 大小超过缓存区，就会出现某包数据一部分进入缓存区，一部分没有（但不会造成丢失，等下一次再进去存区，会造成包被分割了）。
 */
public class demo5_ExampleHalfPackage {

    @Test
    public void mainTest() {
        ByteBuffer buffer = ByteBuffer.allocate(32);
        //                     11            24
        buffer.put("Hello,world\nI'm zhangsan\nHo".getBytes());
        split(buffer);

        // buffer.put("w are you?\nhaha!\n".getBytes());
        // split(buffer);
    }

    private static void split(ByteBuffer buffer) {
        buffer.flip();  // 出buffer 模式
        int oldLimit = buffer.limit();  // limit是buffer原始的数据结尾
        for (int i = 0; i < oldLimit; i++) {  // 遍历buffer中的字节
            if (buffer.get(i) == '\n') {
                // System.out.println(i);  // 第一个 \n 是索引是11
                int capacity = i - buffer.position() + 1;
                ByteBuffer target = ByteBuffer.allocate(capacity);
                // 0 ~ limit
                buffer.limit(i + 1);  // 这里i + 1，估计也是左闭右开的原因，这里设置limit是为了 出buffer 一部分

                target.put(buffer); // 从buffer 读，向 target 写。也可以用下面的循环来进行复制
                // for(int j = 0;j < capacity; j++) {
                //     target.put(buffer.get());
                // }

                debugAll(target);
                buffer.limit(oldLimit);  // limit是buffer原始的数据结尾，需要设置回来，不然下一次循环buffer.get(i)会报错
            }
        }
        buffer.compact();  // 进buffer 模式，这里必须用compact()，因为会把buffer中的剩余格子移动到开头
    }

}
