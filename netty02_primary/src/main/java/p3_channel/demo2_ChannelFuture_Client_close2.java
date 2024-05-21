package p3_channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


@Slf4j
public class demo2_ChannelFuture_Client_close2 {
    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        ChannelFuture channelFuture = new Bootstrap()
                .group(group)
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

        // 在“关闭channel之后”做一些操作的方式2
        ChannelFuture closeFuture = channel.closeFuture();
        closeFuture.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                log.info("关闭之后做一些操作。。。");
                group.shutdownGracefully();
            }
        });

    }
}
