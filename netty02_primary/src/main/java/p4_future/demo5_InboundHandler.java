package p4_future;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;


@Slf4j
public class demo5_InboundHandler {
    public static void main(String[] args) {
        new ServerBootstrap()
                .group(new NioEventLoopGroup())
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                        // 1.通过channel拿到pipeline
                        ChannelPipeline pipeline = nioSocketChannel.pipeline();
                        // 2.添加处理器head -> h1 -> h2 -> h3 -> tail
                        pipeline.addLast("h1", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("h1");
                                ByteBuf buf = (ByteBuf) msg;
                                String aStr = buf.toString(StandardCharsets.UTF_8);
                                super.channelRead(ctx, aStr);
                            }
                        });
                        pipeline.addLast("h2", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("h2");
                                String aStr = (String) msg;
                                String[] strArr = aStr.split("，");
                                super.channelRead(ctx, strArr);
                            }
                        });
                        pipeline.addLast("h3", new ChannelInboundHandlerAdapter() {
                            @Override
                            public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                log.debug("h3");
                                String[] strArr = (String[]) msg;
                                log.debug(Arrays.toString(strArr));
                                // super.channelRead(ctx, strArr);  // 这行代码是入站handler去唤醒下一个入站handler，这里后面没有入站handler了，没必要再唤醒下一个。另外，ctx.fireChannelRead(strArr);和这句代码作用相同。
                                nioSocketChannel.writeAndFlush(ctx.alloc().buffer().writeBytes("你好，客户端...".getBytes(StandardCharsets.UTF_8)));
                            }
                        });

                        pipeline.addLast("h4", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("h4");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h5", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("h5");
                                super.write(ctx, msg, promise);
                            }
                        });
                        pipeline.addLast("h6", new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                                log.debug("h6");
                                super.write(ctx, msg, promise);
                            }
                        });
                    }
                }).bind(8081);
    }
}
