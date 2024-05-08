package socketnio;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import static bytebuffer.ByteBufferUtil.debugCanOutBuffer;

@Slf4j
public class demo8_UDP {
    /*
     * UDP 是无连接的，client 发送数据不会管 server 是否开启
     * server 这边的 receive 方法会将接收到的数据存入 byte buffer，但如果数据报文超过 buffer 大小，多出来的数据会被默默抛弃
     */
    @Test
    public void udpServer() {
        try {
            DatagramChannel channel = DatagramChannel.open();
            channel.socket().bind(new InetSocketAddress(8080));
            System.out.println("监听端口...");
            ByteBuffer buffer = ByteBuffer.allocate(8);
            while (true) {
                channel.receive(buffer);  // UDP协议不会重复接收直到数据接收完成
                buffer.flip();
                debugCanOutBuffer(buffer);
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void udpClient() {
        try {
            DatagramChannel channel = DatagramChannel.open();
            ByteBuffer buffer = StandardCharsets.UTF_8.encode("hello, world!");
            InetSocketAddress address = new InetSocketAddress("localhost", 8080);
            channel.send(buffer, address);
            System.out.println("发送完成，维持客户端不关闭DatagramChannel...");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void tcpServer() {
        try {
            ServerSocketChannel ssc = ServerSocketChannel.open();
            ssc.bind(new InetSocketAddress(8080));
            SocketChannel sc = ssc.accept();
            System.out.println("连接建立...");
            ByteBuffer buffer = ByteBuffer.allocate(8);  // UDP协议不会重复接收，直到数据接收完成
            while (true){
                sc.read(buffer);
                buffer.flip();
                debugCanOutBuffer(buffer);
                buffer.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void tcpClient() {
        try {
            SocketChannel sc = SocketChannel.open();
            sc.connect(new InetSocketAddress("localhost", 8080));
            ByteBuffer buffer = StandardCharsets.UTF_8.encode("hello, world!");
            sc.write(buffer);
            System.out.println("发送完成，维持客户端不关闭SocketChannel...");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 分别运行上面两种连接方式的代码，会发现：
     * TCP连接，如果服务端读取channel到buffer，发现数据超过了buffer的容量，则下一次while循环会读取剩余数据。（这些剩余数据是存在channel中还是存在buffer中，应该不在buffer里，被放在哪里了呢）
     * 而UDP连接，如果服务端读取channel到buffer，发现数据超过了buffer的容量，他好像是一次把channel中数据读取出来，容量之内的放到buffer中，其余的丢掉，下一次while循环什么数据也都读不到了。（我不知道如何实现这种模式）
     */


}
