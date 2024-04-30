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
public class demo1_BlockingServer {

    @Test
    public void server() throws IOException {
        // 使用 nio 来理解阻塞模式, 单线程
        // 0. ByteBuffer
        ByteBuffer buffer = ByteBuffer.allocate(16);
        // 1. 创建了服务器
        ServerSocketChannel ssc = ServerSocketChannel.open();

        // 2. 绑定监听端口
        ssc.bind(new InetSocketAddress(8080));

        // 3. 连接集合
        List<SocketChannel> channels = new ArrayList<>();
        while (true) {
            // 4. accept 建立与客户端连接， SocketChannel 用来与客户端之间通信
            log.debug("connecting...");
            SocketChannel sc = ssc.accept(); // 阻塞，如果没有请求连接发来，线程会暂停在这里等待，ssc.accept()不返回，过一会就会让出CPU，
            log.debug("connected... {}", sc);
            channels.add(sc);
            for (SocketChannel channel : channels) {
                // 5. 接收客户端发送的数据
                log.debug("before read... {}", channel);
                channel.read(buffer); // 阻塞，如果没有请求数据发来，线程会暂停在这里等待，channel.read()不返回（返回值表示读到了多少字节）
                buffer.flip();
                debugCanOutBuffer(buffer);  // debugRead()方法是打印buffer可读取内容，没有进行 出buffer 操作，所以上面buffer.flip()也是可以去掉的。错了！！！！
                // buffer.flip()必须有，因为需要flip()把position移动到buffer可读取内容的第一格，debugRead中会从position开始buffer.get(i)，虽然不进行 出buffer操作，但是需要用到position
                // 为什么debugAll不需要呢，因为debugAll是打印buffer中所有数据，所以position设为0。
                buffer.clear();
                log.debug("after read...{}", channel);
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
