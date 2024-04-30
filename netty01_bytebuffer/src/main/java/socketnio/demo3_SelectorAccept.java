package socketnio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

@Slf4j
public class demo3_SelectorAccept {
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
            ///// 说明：selector是轮询注册到上面的channel（当注册的时候会把channel封装成key，因为后面还要把触发事件类型封装进key），同时还要在注册的时候指定关注这个channel发生的什么类型事件（就是触发事件类型）。
            ///// 然后代码selector.select();就是在执行轮询，当有channel发生事件的时候，就用selector.selectedKeys()获取所有发生事件的channel对应封装的key（key里面还封装着发生了什么事件）。
            // 3.select() 方法，没有事件发生，线程阻塞，有事件，线程才会继续运行
            selector.select();
            // 4.处理事件，selectedKeys内部包含了所有发生的事件
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();  // 代码运行到这里就说明有channel发生事件，然后获取selector中的所有key（key是发生事件的channel和该channel发生的事件的整合）。

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                log.debug("selectedKey：{}", key);

                // 如果这里不处理，那么key中的触发事件还会继续存在，下一次循环遇见selector.select();不会阻塞。当然还有一种对触发事件的处理方式是key.cancel()，但是事件不可以置之不理
                // key.cancel();  // 但是cancel()不仅是对事件的一种处理方式，cancel 还会取消注册在 selector 上的 channel，并从 selectedKeys 集合中删除 key 后续不会再监听事件
                ServerSocketChannel channel = (ServerSocketChannel) key.channel();  // 获取key对应的channel，此代码中selector只有ServerSocketChannel注册上去了，所以它只轮询ServerSocketChannel，而且触发事件是accept事件
                SocketChannel sc = channel.accept();

                log.debug("连接建立{}", sc);
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
