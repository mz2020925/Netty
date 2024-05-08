package p1_helloworld;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;

public class demo1_HelloWorld_Server {

    public static void main(String[] args) {
        new ServerBootstrap()  // 1.服务器启动器，负责组装相关netty组件，启动服务器
            .group(new NioEventLoopGroup())  // 2.创建一个组，里面有各种EventLoop，类似BossEventLoop（一个Selector，一个Thread）, WorkerEventLoop（Selector，Thread）
            .channel(NioServerSocketChannel.class)  // 3.设置服务器的channel类型是NIO的ServerSocketChannel（netty对ServerSocketChannel进行了封装），相当于 ServerSocketChannel ssc = ServerSocketChannel.open(); ssc.configureBlocking(false);
            .childHandler(  // 4.在这里面设置服务器的channel需要处理的事情（它本身也是一个Handler，负责添加别的Handler），例如需要处理接受连接、需要处理读出channel到buffer、需要处理从buffer读入channel……，下面首先要进行初始化
                new ChannelInitializer<NioSocketChannel>() {  // 5.new ChannelInitializer<NioSocketChannel>()代表服务器和客户端建立的通道，这里是对这个channel进行的初始化操作 —— 添加一些handler
                    @Override
                    protected void initChannel(NioSocketChannel nsc) {
                        nsc.pipeline().addLast(new StringDecoder());  // 6.1 添加具体的handler，这里的new StringDecoder()是把ByteBuf转换成字符串
                        nsc.pipeline().addLast(new SimpleChannelInboundHandler<String>() {  // 6.2 添加具体的handler，这里的new SimpleChannelInboundHandler<String>()是读取channel的
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, String msg) {
                                System.out.println(msg);
                            }
                        });
                    }
                }
            )
            .bind(8081);  // 7.服务器工作在8080端口
    }



}
