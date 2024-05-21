package p4_future;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Scanner;


@Slf4j
public class demo4_Client {
    public static void main(String[] args) {
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

        // 2.向服务器发送数据，这次直接不用主线程在连接建立之后发送数据，调用另一个线程监听并发送数据
        // 主线程调用addListener方法，会创建一个线程，去监听异步方法connect()的结果，如果连接上了，这个线程就会执行operationComplete方法
        channelFuture.addListener(new ChannelFutureListener() {
            @Override  // 当连接建立之后就会执行下面的函数
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                Channel channel = channelFuture.channel();
                log.debug("{}", channel);
                channel.writeAndFlush("你好，服务端...");
                InputStream in = System.in;
                Scanner scanner = new Scanner(in);
                while (true){
                    String line = scanner.nextLine();
                    if(line.equals("q")){
                        break;
                    }
                    channel.writeAndFlush(line);
                }
            }
        });
    }
}
