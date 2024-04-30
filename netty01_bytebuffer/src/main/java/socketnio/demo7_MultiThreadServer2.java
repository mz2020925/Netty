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

import static bytebuffer.ByteBufferUtil.debugAll;


@Slf4j
public class demo7_MultiThreadServer2 {
    @Test
    public void multiThreadServer() throws IOException {
        Thread.currentThread().setName("boss");  // 这里获取主线程，并给主线程起个名字
        ServerSocketChannel ssc = ServerSocketChannel.open();  // 这个sscChannel由主线程boss管理
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        ssc.register(boss, SelectionKey.OP_ACCEPT, null);
        ssc.bind(new InetSocketAddress(8080));

        // 1.创建固定数量（这里就1个）的worker 并初始化（new关键字会 进行 类的初始化阶段）
        Worker worker = new Worker("worker-0");
        // worker.createThread();
        while (true) {
            boss.select();
            log.info("有sscChannel 发生了 触发事件");
            Iterator<SelectionKey> iter = boss.selectedKeys().iterator();
            while (iter.hasNext()) {
                SelectionKey key = iter.next();
                iter.remove();
                if (key.isAcceptable()) {
                    SocketChannel sc = ssc.accept();  // boss 获取连接
                    log.info("线程 {} 管理的sscChannel发生了Acceptable事件，请求来自：{}", Thread.currentThread().getName(), sc.getRemoteAddress());
                    sc.configureBlocking(false);
                    worker.createOrUseThread(sc);  // 这行代码放在这里是不对的，因为这样的话，每来一个请求连接，就会创建一个线程。错了！！！因为if(!start) {} 中的代码只会在第一次调用的时候执行一遍
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

        // 创建线程，创建该线程的selector。下面这个方法中的代码是运行在boss线程中的
        public void createOrUseThread(SocketChannel sc) throws IOException {
            if (!start) {  // start的作用是保证createThread()方法第一次被调用的时，if条件范围内的代码执行一次。后面再调用createThread()方法也不会在执行了
                selector = Selector.open();
                thread = new Thread(this, name);  // 第一个参数this表示，后面我们调用thread.start()的时候就回去this类中执行run()方法
                thread.start();
                start = true;
            }
            // 2.创建一个任务放入队列：将建立的scChannel注册到work-0线程的selector上，意思就是这个scChannel由work-0管理
            sc.register(selector, SelectionKey.OP_READ, null);
            selector.wakeup();  // 让selector跳过一次阻塞。
            // 如果我把queue.add()操作放到if (!start){} 之前，再把queue.poll()操作放到selector.select();之前就可以不用唤醒了吧。
            // 不可以，因为如果有一个客户端发来请求建立，就会发生虽然任务加入队列，单词worker-0却阻塞在selector.select();，它前面的queue.poll()操作不能得到及时执行。所以必须queue.poll()。
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
                            debugAll(buffer);
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
