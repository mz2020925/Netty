package socketnio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;

@Slf4j
public class demo6_SelectorChannelWrite2 {

    @Test
    public void server() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT, null);

        while (true) {
            selector.select();
            log.info("有Channel发生了触发事件");
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                log.info("发生触发事件的Channel对应的SelectionKey: {}", key);
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();  // 因为accept触发事件只能发生在ServerSocketChannel这个channel中
                    log.info("连接已建立：{}", sc);
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ, null);  // 先在SocketChannel加上可读监听事件

                    // 1. 生成待向客户端发送内容
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 30_000_000; i++) {
                        sb.append("a");
                    }

                    // 2. 先向客户端发送一次数据，返回值代表实际写入的字节数
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    int count = sc.write(buffer);  // 这里和demo8_SelectorChannelWrite的区别就在于这里没有了while循环，线程不会一直暂停在这里

                    System.out.println("服务端本次发送字节数：" + count);
                    // 4. 如果有剩余字节未发送，才需要给SocketChannel加上可写监听事件
                    if (buffer.hasRemaining()) {
                        // read: 1（0001） , write: 4（0100）
                        // 在原有可读事件的基础上，再在SocketChannel加上可写监听事件
                        scKey.interestOps(scKey.interestOps() ^ SelectionKey.OP_WRITE);  // 0001 ^ 0100 = 0101
                        // 因为后面还要把buffer中的数据写到SocketChannel中，所以需要把 buffer 作为附件加入SocketChannel 对应的 scKey，
                        // 这样后面才能找到这个buffer
                        scKey.attach(buffer);
                    }
                } else if (key.isWritable()) {
                    SocketChannel sc = (SocketChannel) key.channel();  // 拿到key对应的channel
                    ByteBuffer buffer = (ByteBuffer) key.attachment();  // 拿到key对应的buffer
                    int count = sc.write(buffer);
                    System.out.println("服务端本次发送字节数：" + count);
                    if (!buffer.hasRemaining()) {  // buffer中数据已经全部写如SocketChannel
                        // SocketChannel不用再包含可写监听事件
                        key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);  // 0101 ^ 0100 = 0001
                        // SocketChannel不再需要这个buffer了
                        key.attach(null);
                    }
                }
                // 当我们在客户端完成数据接收之后，关闭客户端的时候，客户端会给服务端发送一个数据，服务端的Channel又会发生可读事件，所以严格来说这里必须处理。总的来说这个文件写的代码有点乱。
                // 在demo8_SelectorChannelWrite3中会把key.isReadable()的代码加上。
                // else if(key.isReadable()) {
                //
                // }
            }
        }
    }


    @Test
    public void client() throws IOException {
        SocketChannel sc = SocketChannel.open();
        sc.connect(new InetSocketAddress("localhost", 8080));
        // 3.接收数据
        int count = 0;
        while (true) {
            ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
            int temp = sc.read(buffer);
            count += temp;
            System.out.printf("客户端本次接收字节数：%d, 总共接收字节数：%d\n", temp, count);
            buffer.clear();
        }
    }
}
