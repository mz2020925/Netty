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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static bytebuffer.ByteBufferUtil.debugAll;

/**
 * 多个worker
 */
@Slf4j
public class demo7_MultiThreadServer4 {
    @Test
    public void multiThreadServer() throws IOException {
        Thread.currentThread().setName("boss");  // 这里获取主线程，并给主线程起个名字
        ServerSocketChannel ssc = ServerSocketChannel.open();  // 这个sscChannel由主线程boss管理
        ssc.configureBlocking(false);
        Selector boss = Selector.open();
        ssc.register(boss, SelectionKey.OP_ACCEPT, null);
        ssc.bind(new InetSocketAddress(8080));

        // 1.创建固定数量（2个）的worker 并初始化（new关键字会 进行 类的初始化阶段）
        Worker[] workers = new Worker[2];
        for (int i = 0; i < workers.length; i++) {
            workers[i] = new Worker("worker-" + i);
        }
        AtomicInteger index = new AtomicInteger();
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
                    // 把这个channel交给某个worker处理
                    workers[index.getAndIncrement() % workers.length].createOrUseThread(sc);  // 这行代码放在这里是不对的，因为这样的话，每来一个请求连接，就会创建一个线程。错了！！！因为if(!start) {} 中的代码只会在第一次调用的时候执行一遍
                }
            }
        }
    }

    static class Worker implements Runnable {
        private Thread thread;
        private Selector selector;
        private String name;
        private volatile boolean start = false;
        private ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();


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
            queue.add(() -> {
                try {
                    // 2.创建一个任务放入队列：将建立的scChannel注册到work-0线程的selector上，意思就是这个scChannel由work-0管理
                    sc.register(selector, SelectionKey.OP_READ, null);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            selector.wakeup();  // 如果selector正处于阻塞，则跳出阻塞；如果selector没出阻塞，让selector下一次将要阻塞的时候跳过一次阻塞。
            // 如果我把queue.add()操作放到if (!start){} 之前，再把queue.poll()操作放到selector.select();之前就可以不用唤醒了吧。
            // 不可以，因为如果有一个客户端发来请求建立，就会发生虽然任务加入队列，单词worker-0却阻塞在selector.select();，它前面的queue.poll()操作不能得到及时执行。所以必须queue.poll()。
        }

        @Override
        public void run() {
            while (true) {
                try {
                    // B站弹幕：为什么不直接传sc，然后在worker线程取出来调用register（） ？传一个runnable，这一样是异步的，万一runnable的代码没执行完，worker线程转了一圈又阻塞了，那不是白给？
                    // B站弹幕：利用队列解耦合
                    // B站弹幕：有点js里回调函数内味了
                    // B站弹幕：之前被select阻塞的原因是因为channel是boss线程完成注册的,worker线程无法感知,所以会被阻塞,现在channel注册处理通知都是由worker线程完成的

                    selector.select();
                    Runnable task = queue.poll();
                    if (task != null) {
                        task.run();
                    }

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
        try {
            SocketChannel sc = SocketChannel.open();
            sc.connect(new InetSocketAddress("localhost", 8080));
            sc.write(StandardCharsets.UTF_8.encode("hello, world!"));
            System.in.read();  // 加上这个是为了让这个方法不结束，如果不加这个，这个方法正常结束，客户端会因此关闭，服务端那边就会报错”远程主机强迫关闭了一个主机“，同时scChannel会一直发生可读触发事件
            // 如果不加这个，用try-with-resource，客户端会正常断开连接，服务端那边不会报错，同时scChannel会一直发生可读触发事件
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
