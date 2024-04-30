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

import static bytebuffer.ByteBufferUtil.debugCanOutBuffer;


@Slf4j
public class demo6_SelectorChannelWrite3 {
    @Test
    public void server() throws IOException {
        // 1. 生成待向客户端发送内容
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 30_000_000; i++) {
            sb.append("a");
        }
        ByteBuffer strBuffer = Charset.defaultCharset().encode(sb.toString());


        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false);
        ssc.bind(new InetSocketAddress(8080));

        Selector selector = Selector.open();
        ssc.register(selector, SelectionKey.OP_ACCEPT, null);

        while (true) {
            selector.select();
            log.info("有Channel [发生了] 触发事件");
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();  // 因为accept触发事件只能发生在ServerSocketChannel这个channel中
                    log.info("ServerSocketChannel [发生了] Acceptable事件，请求来自：{}", sc.getRemoteAddress());
                    sc.configureBlocking(false);
                    SelectionKey scKey = sc.register(selector, SelectionKey.OP_READ ^ SelectionKey.OP_WRITE, null);// 先在SocketChannel加上可读监听事件

                    // 2. 向客户端发送一次数据，返回值代表实际写入的字节数 —— 这里其实也是给Channel一个Write触发事件
                    int count = sc.write(strBuffer);

                    log.info("服务端本次发送字节数：{}", count);
                    // 4. 如果有剩余字节未发送，才需要给SocketChannel加上可写监听事件
                    if (strBuffer.hasRemaining()) {
                        // read: 1（0001） , write: 4（0100）
                        // 在原有可读事件的基础上，再在SocketChannel加上可写监听事件
                        scKey.interestOps(scKey.interestOps() | SelectionKey.OP_WRITE);  // 0001 ^ 0100 = 0101
                        // 因为后面还要把buffer中的数据写到SocketChannel中，所以需要把 buffer 作为附件加入SocketChannel 对应的 scKey，
                        // 这样后面才能找到这个buffer
                        scKey.attach(strBuffer);
                    }  // 那如果没有剩余呢，触发事件会自动结束吗？？？

                } else if (key.isWritable()) {
                    log.info("SocketChannel [发生了] Writable事件");
                    try {
                        SocketChannel sc = (SocketChannel) key.channel();  // 拿到key对应的channel
                        ByteBuffer attachment = (ByteBuffer) key.attachment();  // 拿到key对应的buffer
                        int count = sc.write(strBuffer);
                        log.info("服务端本次发送字节数：{}", count);
                        // 4. 如果没有剩余字节，让SocketChannel的不再监听可写事件（也就说触发事件不会自动结束吗？？？）
                        if (!strBuffer.hasRemaining()) {
                            key.interestOps(key.interestOps() ^ SelectionKey.OP_WRITE);  // 0101 ^ 0100 = 0001
                            key.attach(null);
                        }
                    } catch (IOException e) {  // 客户端异常断开
                        key.cancel();
                        e.printStackTrace();
                    }
                }
                else if (key.isReadable()) {  // SocketChannel在创建的时候就会监听可读事件和可写事件
                    log.info("SocketChannel [发生了] Readable事件");
                    try {
                        SocketChannel sc = (SocketChannel) key.channel();  // 拿到key对应的channel
                        ByteBuffer buffer = ByteBuffer.allocate(16);  // 拿到key对应的buffer
                        int count = sc.read(buffer);
                        if (count != -1) {  // SocketChannel中数据已经全部读取到 buffer
                            log.info("服务端本次接收字节数：{}", count);
                            // 下面是 出buffer，看看buffer中是什么
                            buffer.flip();
                            debugCanOutBuffer(buffer);
                        } else {
                            key.cancel();
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
    public void client() throws IOException {
        Selector selector = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);
        sc.connect(new InetSocketAddress("localhost", 8080));
        int count = 0;
        while (true) {
            selector.select();
            Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isConnectable()) {
                    sc.finishConnect();  // 完成连接建立
                    log.info("连接已建立，请求发给：{}", sc.getRemoteAddress());
                } else if (key.isReadable()) {
                    ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
                    int temp = sc.read(buffer);
                    count += temp;
                    log.info("客户端本次接收字节数：{}, 总共接收字节数：{}", temp, count);
                    buffer.clear();
                }
            }
        }
    }
}
