package socketnio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import static bytebuffer.ByteBufferUtil.debugCanOutBuffer;


@Slf4j
public class demo7_MultiThreadServer {
    @Test
    public void multiThreadServer() throws IOException {
        Thread.currentThread().setName("boss");  // 这里获取主线程，并给主线程起个名字
        ServerSocketChannel ssc = ServerSocketChannel.open();  // 这个sscChannel由主线程boss管理
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        SelectionKey bossKey = ssc.register(boss, 0, SelectionKey.OP_ACCEPT);
        ssc.bind(new InetSocketAddress(8080));

        // 1.创建固定数量的worker 并初始化（new关键字会 进行 类的初始化阶段）
        Worker worker = new Worker("worker-0");
        worker.createOrUseThread();
        while (true) {
            boss.select();
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();  // boss 获取连接
                    log.info("线程 {} 管理的sscChannel发生了Acceptable事件，请求来自：{}", Thread.currentThread().getName(), sc.getRemoteAddress());
                    sc.configureBlocking(false);
                    // 2.将建立的Channel注册到work-0线程的selector上，意思就是这个channel由work-0管理
                    sc.register(worker.selector, SelectionKey.OP_READ, null);
                    // 上面那行代码想把channel注册到 work-0 线程的selector，但是worker.createThread();会thread.start();执行run()方法中的selector.select();，
                    // 就是说work-0线程正在执行（worker.）selector.select()，处于阻塞状态，而boss线程想要执行sc.register(worker.selector, SelectionKey.OP_READ, null);。阻塞状态下是不可以注册的。

                    // ****** 问题抽象出来就是：两个线程都要使用某个变量，必须都有机会拿到变量，不能一个线程独占着，才能满足我们的目的。
                    // ****** 在这里就是不能一直阻塞在 work-0 中的 run(){ selector.select(); } ，要让阻塞状态结束，只有处于非阻塞状态下，boss线程才有机会执行 sc.register(worker.selector, SelectionKey.OP_READ, null);
                    // 在demo7_MultiThreadServer2.java和demo7_MultiThreadServer3.java（推荐）中会解决这个问题。
                    // ****** 注意：方法被哪个线程调用，方法内的代码就会被哪个线程执行。
                }
            }
        }
    }

    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false;


        public Worker(String name) {
            this.name = name;
        }

        // 创建线程，创建该线程的selector
        public void createOrUseThread() throws IOException {
            if (!start) {  // start的作用是保证register()方法第一次被调用的时，if条件范围内的代码执行一次。后面再不小心调用了register()方法也不会在执行了
                thread = new Thread(this, name);  // 第一个参数this表示，后面我们调用thread.start()的时候就回去this类中执行run()方法
                selector = Selector.open();
                thread.start();
                start = true;
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    selector.select();
                    Iterator<SelectionKey> iter = selector.selectedKeys().iterator();
                    while (iter.hasNext()) {
                        SelectionKey key = iter.next();
                        iter.remove();
                        if (key.isReadable()) {
                            SocketChannel channel = (SocketChannel) key.channel();
                            log.info("线程 {} 管理的scChannel发生了Readable事件，数据来自：{}", Thread.currentThread().getName(), channel.getRemoteAddress());
                            ByteBuffer buffer = ByteBuffer.allocate(16);
                            channel.read(buffer);
                            buffer.flip();
                            debugCanOutBuffer(buffer);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void client() {
        try (SocketChannel sc = SocketChannel.open()) {
            sc.connect(new InetSocketAddress("localhost", 8080));
            sc.write(StandardCharsets.UTF_8.encode("hello, world!"));
            System.out.println("close...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
