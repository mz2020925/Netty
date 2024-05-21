package p3_channel;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;


@Slf4j
public class demo1_ChannelFuture_Client_sync {
    public static void main(String[] args) throws InterruptedException {
        ChannelFuture channelFuture = new Bootstrap()
                .group(new NioEventLoopGroup(2))
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast(new StringEncoder());  // 意思就是客户端给的是一个字符串，这里需要编码成
                    }
                })
                // 1.连接到服务器，异步非阻塞，main发起了调用，真正执行connect的是nio线程
                .connect(new InetSocketAddress("localhost", 8081));

        // channelFuture.sync();  // 主线程阻塞在这里，当另一个线程connect建立完成后，主线程再往下执行 —— 可是这似乎怎么做到的呢？？？
        // 注释上面的代码，无阻塞立刻向下执行获取channel，可能获取不到连接
        Channel channel = channelFuture.channel();
        log.debug("{}", channel);
        // 2.向服务器发送数据
        channel.writeAndFlush("hello world!");





    }
}
