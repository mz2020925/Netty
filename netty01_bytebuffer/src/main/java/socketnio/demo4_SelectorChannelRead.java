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
import java.util.Iterator;

import static bytebuffer.ByteBufferUtil.debugCanOutBuffer;

/**
 * ServerSocketChannel会出现isAcceptable触发事件。
 * <p>
 * SocketChannel会出现isReadable触发事件和isWriteable触发事件。
 * <p>
 * Connect事件发生在客户端，和服务端没有关系。
 * <p>
 * 关闭连接SocketChannel是客户端的事情，服务端不能主动关闭SocketChannel
 */
@Slf4j
public class demo4_SelectorChannelRead {
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
            // 4.处理事件，selectedKeys内部包含了所有发生了事件的channel对应的key
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();  // 代码运行到这里就说明有channel发生事件，然后获取selector中的所有key（key是发生事件的channel和该channel发生的事件的整合）。

            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();  // 拿到这个key时，接着就把key从集合中删除，否则下次就会拿到一个key（它的触发事件已经被处理）
                log.debug("selectedKey：{}", key);
                // 5.区分事件类型
                if (key.isAcceptable()) {  // key.isAcceptable()表示这是一个处理accept触发事件的key
                    // 如果这里不处理，那么channel中的触发事件还会继续存在，下一次循环遇见selector.select();不会阻塞。当然还有一种对触发事件的处理方式是key.cancel()，但是事件不可以置之不理
                    // key.cancel();
                    ServerSocketChannel channel = (ServerSocketChannel) key.channel();  // 获取key对应的channel，此代码中selector只有ServerSocketChannel注册上去了，所以它只轮询ServerSocketChannel，而且触发事件是accept事件
                    SocketChannel sc = channel.accept();
                    log.debug("连接建立：{}", sc);
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, 0, null);
                    scKey.interestOps(SelectionKey.OP_READ);
                } else if (key.isReadable()) {  // key.isAcceptable()表示这是一个处理 进buffer 触发事件的key
                    SocketChannel channel = (SocketChannel) key.channel();
                    ByteBuffer buffer = ByteBuffer.allocate(16);
                    channel.read(buffer);
                    buffer.flip();
                    debugCanOutBuffer(buffer);
                    /* 这里isReadable事件的处理不完善，后面demo4_SelectorChannelRead2.java还会完善  */
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
