package p3_channel;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;


@Slf4j
public class demo1_channelFuture_Server {
    public static void main(String[] args) {
        EventLoopGroup defaultGroup = new DefaultEventLoopGroup(1);
        new ServerBootstrap()
                .group(new NioEventLoopGroup(1), new NioEventLoopGroup(2))
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        nioSocketChannel.pipeline().addLast("handler1", new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                ByteBuf buf = (ByteBuf) msg;
                                // 拿到ByteBuf之后，就可以处理了
                                String msgStr = buf.toString(StandardCharsets.UTF_8);
                                log.debug(msgStr);
                                ctx.fireChannelRead(msgStr);
                            }
                        }).addLast(defaultGroup, "default_handler", new ChannelInboundHandlerAdapter(){
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                String str = (String)msg;
                                log.debug(str);
                            }
                        });
                    }
                }).bind(8081);
    }


}
