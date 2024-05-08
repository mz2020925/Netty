package p2_eventloop;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class demo3_ServerEventLoop_BossWorker {
        public static void main(String[] args) {
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
                        nioSocketChannel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
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
