package p2_eventloop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;


@Slf4j
public class demo3_ServerEventLoop_BossWorkerDefault {
    // 将worker的EventLoopGroup进一步进行细分，为什么要进一步细分，因为一个channel多个handler，如果有一个handler非常耗时，把这种handler单独交给的单独的一个DefaultEventLoopGroup处理
    // 这个DefaultEventLoopGroup不归Boss的EventLoopGroup管理，不会用来监听channel的读写，这里只是用来提供一个handler而已。

    public static void main(String[] args) {
        EventLoopGroup defaultGroup = new DefaultEventLoopGroup(1);
        new ServerBootstrap()
                // 1个Boss的EventLoop，2个Worker的EventLoop
                // boss只负责NioServerSocketChannel上发生的accept事件，只负责发生在8081端口的accept事件，如果服务器运行在多个端口，boss也要有多个
                // worker只负责NioSocketChannel上发生的read、write等事件
                // 第1个参数就是boss的EventLoop，它的nThreads可以不用显式的设置为1
                .group(new NioEventLoopGroup(1), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast("handler1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug(buf.toString(StandardCharsets.UTF_8));
                                ctx.fireChannelRead(msg);  // 把msg传给handler链的下一个
                            }
                        }).addLast(defaultGroup, "default_handler", new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                log.debug(buf.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                }).bind(8081);
    }
}
