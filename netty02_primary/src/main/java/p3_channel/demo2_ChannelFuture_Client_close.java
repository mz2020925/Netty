package p3_channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


@Slf4j
public class demo2_ChannelFuture_Client_close {
    public static void main(String[] args) throws InterruptedException {
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup(2))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new LoggingHandler(LogLevel.DEBUG));
                        nioSocketChannel.pipeline().addLast(new StringEncoder());  // 意思就是客户端给的是一个字符串，这里需要编码成
                    }
                })
                // 1.连接到服务器，异步非阻塞，main发起了调用，真正执行connect的是nio线程
                .connect(new InetSocketAddress("localhost", 8081));

        channelFuture.sync();
        Channel channel = channelFuture.channel();
        log.debug("连接已建立，获取channel：{}", channel);
        channel.writeAndFlush("hello world!");

        channel.close();  // 这也是异步方法

        // 在“关闭channel之后”做一些操作的方式1
        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.sync();  // 确保后面代码是在关闭channel之后在执行的

        log.info("关闭之后做一些操作。。。");
    }
}
