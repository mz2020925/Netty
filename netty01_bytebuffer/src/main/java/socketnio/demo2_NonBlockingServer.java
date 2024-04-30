package socketnio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

import static bytebuffer.ByteBufferUtil.debugCanOutBuffer;

@Slf4j
public class demo2_NonBlockingServer {
    @Test
    public void server() throws IOException {
        // 使用 nio 来理解非阻塞模式, 单线程
        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 1. 创建了服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.configureBlocking(false); // 非阻塞模式
        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8080));
        // 3. 连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            // 4. accept 建立与客户端连接， SocketChannel 用来与客户端之间通信
            SocketChannel sc = ssc.accept();  // 非阻塞，如果没有请求连接发来，线程会继续运行，ssc.accept()返回null
            if (sc != null) {
                log.debug("connected... {}", sc);
                sc.configureBlocking(false);  // 非阻塞模式
                channels.add(sc);
            }
            for (SocketChannel channel : channels) {
                // 5. 接收客户端发送的数据
                int count = channel.read(buffer);  // 非阻塞，如果没有请求数据发来，线程会继续运行，channel.read()返回 0（返回值表示读到了多少字节）
                if (count > 0) {
                    buffer.flip();
                    debugCanOutBuffer(buffer);  // 打印 可出buffer 的数据
                    buffer.clear();
                    log.debug("after read...{}", channel);
                }
            }
        }  // 在这个《非阻塞单线程模式》下，while一直在循环（这样也是不好的），不会停下来，每次循环都会看看有没有客户端请求连接发来、有没有客户端请求数据发来
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
