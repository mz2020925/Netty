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

import static bytebuffer.ByteBufferUtil.debugAll;

/**
 * 前面我们的buffer都是设置16字节，当数据超过16字节就会进行多次接收，对于英文字符这没有问题，因为英文字符一个字符对应一个字节。但是对于的话，一个汉字对应3个字节（utf-8编码），就会出现一个的汉字的3个字节分两次接收，这就会出现半包问题。
 * 如果我们不给包加上结束符，那么可能会出现两个包的数据都被存入buffer中了（包数据比较小），那么读取的时候就会分不清。这就会出现黏包问题。
 * <p>
 * 解决黏包问题，就是在数据结尾加上\n这样的分隔符，解决半包问题，就需要每个channel都有一个自己的buffer，还得用到split和自动扩容。
 * <p>
 * 下面代码的作用是解决黏包、半包、和容量不够的问题。也就是处理消息边界问题。（对于处理消息边界问题，不管是nio还是bio，只要tcp编程，都要考虑这个问题。）
 * 思路是按分隔符拆分，缺点是效率低，下面的代码就是这个思路，解决黏包半包问题都需要用split按照 \n 分割，加入自动扩容的代码，解决buffer容量不够的问题（容量不够就是连一个包都放不下，自然会出现半包）。
 * netty在上面代码中提到的自动扩容问题，处理的更加精细。比如这里没有缩容操作，是不合理的。
 */
@Slf4j
public class demo5_SelectorHalfPackage {
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
                    // 将一个ByteBuffer作为附件关联到selectionKey上
                    SelectionKey scKey = sc.register(selector, 0, ByteBuffer.allocate(16));
                    scKey.interestOps(SelectionKey.OP_READ);
                } else if (key.isReadable()) {  // key.isAcceptable()表示这是一个处理 进buffer 触发事件的key
                    try {
                        SocketChannel sc = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        int count = sc.read(buffer);
                        if (count == -1) {
                            key.cancel();  // 数据已经传输完成（客户端可能已经关闭channel），这个channel就不需要了。cancel 会取消注册在 selector 上的 channel，并从 selectedKeys 集合中删除 此channel对应的key
                        } else {
                            split(buffer);
                            if (buffer.position() == buffer.limit()) {  // 说明 split(buffer) 中没有遇见\n，需要扩容
                                ByteBuffer newBuffer = ByteBuffer.allocate(buffer.capacity() * 2);
                                buffer.flip();  // 切换为 出buffer模式
                                newBuffer.put(buffer);
                                key.attach(newBuffer);
                            }
                        }
                    } catch (IOException e) {
                        key.cancel();
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private static void split(ByteBuffer buffer) {
        buffer.flip();  // 出buffer 模式
        int oldLimit = buffer.limit();  // limit是buffer此时的数据结尾
        for (int i = 0; i < oldLimit; i++) {  // 遍历buffer中的字节
            if (buffer.get(i) == '\n') {
                // System.out.println(i);  // 第一个 \n 是索引是11
                int capacity = i - buffer.position() + 1;
                ByteBuffer target = ByteBuffer.allocate(capacity);
                // 0 ~ limit
                buffer.limit(i + 1);  // 这里i + 1，估计也是左闭右开的原因

                target.put(buffer); // 从buffer 读，向 target 写。也可以用下面的循环来进行复制
                // for(int j = 0;j < capacity; j++) {
                //     target.put(buffer.get());
                // }

                debugAll(target);  // 这里就可以对这一段数据进行处理
                buffer.limit(oldLimit);
            }
        }
        buffer.compact();  // 切换为 进buffer 模式，这里必须用compact()，因为会把buffer中的剩余格子移动到开头
    }


    @Test
    public void client() {
        try (SocketChannel sc = SocketChannel.open()) {
            sc.connect(new InetSocketAddress("localhost", 8080));
            System.out.println("waiting...");
            sc.write(Charset.defaultCharset().encode("0123\n456789abcdef"));
            sc.write(Charset.defaultCharset().encode("0123456789abcdef3333\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
