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
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static bytebuffer.ByteBufferUtil.debugCanOutBuffer;


@Slf4j
public class demo4_SelectorChannelRead2 {
    @Test
    public void server() throws IOException {
        // 1.创建selector，管理多个channel
        Selector selector = Selector.open();
        ServerSocketChannel ssc = ServerSocketChannel.open();  // 获取ServerSocketChannel
        ssc.configureBlocking(false);
        // 2.建立selector 和 ServerSocketChannel 的联系（注册）
        SelectionKey sscKey = ssc.register(selector, 0, null);  // SelectionKey就是将来事件发生后，通过它可以知道事件是什么，以及哪个ServerSocketChannel发生了事件
        sscKey.interestOps(SelectionKey.OP_ACCEPT);  // 设置这个sscKey只关注accept事件
        ssc.bind(new InetSocketAddress(8080));  // ServerSocketChannel监听端口


        while (true) {
            // 3.select() 方法，没有事件发生，线程阻塞，有事件，线程才会继续运行
            selector.select();
            // 4.处理事件，selectedKeys内部包含了所有发生了事件的channel对应的key
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();  // 代码运行到这里就说明有channel发生事件，然后获取selector中的所有key（key是发生事件的channel和该channel发生的事件的整合）。

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();  // 拿到这个key时，接着就把key从集合中删除，否则下次就会拿到一个key（它的触发事件已经被处理）
                log.debug("selectedKey：{}", key);
                // 5.区分事件类型
                if (key.isAcceptable()) {  // key.isAcceptable()表示这是一个处理accept触发事件的key
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();  // 获取key对应的channel，此代码中selector只有ServerSocketChannel注册上去了，所以它只轮询ServerSocketChannel，而且触发事件是accept事件
                    SocketChannel sc = channel.accept();
                    log.debug("连接建立：{}", sc);
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                } else if (key.isReadable()) {  // key.isAcceptable()表示这是一个处理 进buffer 触发事件的key
                    try {
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = ByteBuffer.allocate(16);
                        int count = sc.read(buffer);
                        if (count == -1) {
                            key.cancel();  // 数据已经传输完成（客户端可能已经关闭channel），这个channel就不需要了。cancel 会取消注册在 selector 上的 channel，并从 selectedKeys 集合中删除 此channel对应的key
                        } else {
                            buffer.flip();
                            debugCanOutBuffer(buffer);
                            System.out.println(StandardCharsets.UTF_8.decode(buffer));
                        }
                    } catch (IOException e) {
                        key.cancel();
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Test
    public void client() {
        try (SocketChannel sc = SocketChannel.open()) {
            sc.connect(new InetSocketAddress("localhost", 8080));
            System.out.println("waiting...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
