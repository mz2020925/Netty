package p2_eventloop;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

public class demo3_ClientEventLoop {
    public static void main(String[] args) throws InterruptedException {
        Channel channel = new Bootstrap()  // 1.客户端启动器，负责组装相关netty组件，启动客户端
                .group(new NioEventLoopGroup())  // 2.创建一个组，里面有各种EventLoop（一个Selector，一个Thread）
                .channel(NioSocketChannel.class)  // 3.设置客户端的channel类型是NIO的ServerSocketChannel
                .handler(  // 4.在这里面设置客户端的channel需要处理的事情，下面首先要进行初始化
                        new ChannelInitializer<NioSocketChannel>() {  // 5.将channel初始化之后。再给channel添加一个个handler
                            @Override
                            protected void initChannel(NioSocketChannel nsc) throws Exception {
                                nsc.pipeline().addLast(new StringEncoder());  // 6.1 添加具体的handler，这里的new StringEncoder()是把字符串转换成ByteBuf
                            }
                        })
                .connect("localhost", 8081)  // 7.客户端向哪个服务器发出建立连接请求
                .sync()  // 8.没有建立起连接，线程就阻塞在这里
                .channel();

        System.out.println("加断点发送数据...");
        // channel.writeAndFlush("hello, world!");  // 9.连接建立起之后，就使用channel的方法writeAndFlush()向服务器发送信息。
    }
}

