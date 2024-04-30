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
public class demo6_SelectorChannelWrite {
    @Test
    public void server() throws IOException {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        Selector selector = Selector.open();
        // SelectionKey sscKey = ssc.register(selector, 0, null);
        // sscKey.interestOps(SelectionKey.OP_ACCEPT);
        // 上面两行代码的作用 和下面这行代码等效
        ssc.register(selector, SelectionKey.OP_ACCEPT, null);

        while (true) {
            selector.select();

            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                log.info("selectedKey: {}", key);
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();  // 因为accept触发事件只能发生在ServerSocketChannel这个channel中
                    log.info("连接已建立：{}", sc);
                    sc.configureBlocking(false);
                    // 1. 生成待向客户端发送内容
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < 30_000_000; i++) {
                        sb.append("a");
                    }
                    ByteBuffer buffer = Charset.defaultCharset().encode(sb.toString());
                    while (buffer.hasRemaining()){
                        // 2.向客户端发送数据，返回值代表实际写入的字节数
                        int write = sc.write(buffer);
                        System.out.println("服务端本次发送字节数：" + write);
                    }
                }
            }
        }
    }
    /**
     * 上述代码其实不满足非阻塞的思想，即使我们把ServerSocketChannel和SocketChannel都设置为非阻塞模式，但是并不是我们想要的那种非阻塞。
     * 因为上述代码在把一块大量数据发完之前是不会从 while (buffer.hasRemaining()){...} 这个循环跳出的，这段时间线程一直在处理一个SocketChannel的发送数据操作。处理不完就不会干其他事儿。
     * 后面demo6_SelectorChannelWrite2.java 会进行改善。
     * 改善的地方在于线程不会一直处理一个SocketChannel的发送数据操作，如果当前无法发送数据，线程还可以去读取数据。这样才符合非阻塞的思想。
     */


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
