package vs_nio_bio_aio;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static bytebuffer.ByteBufferUtil.debugAll;


/**
 * 同步：线程自己去获取结果（一个线程）
 * 异步：线程自己不去获取结果，而是由其它线程送结果（至少两个线程）
 *
 * B站弹幕：同步是指发起消息后，主动等待消息结果。异步是指发起消息后，被动通知消息结果。
 *
 * 在回调方法里将结果写入某个变量,然后用户线程从这个变量里获取结果。用户线程中有一个引用变量res，
 * 用户线程新建一个线程（这是一个守护线程）去执行运行回调方法，回调方法的参数就是用户线程里res，
 * 回调方法里面进行读取操作，并把结果存到res中，通知用户线程结果存好了，用户线程再去使用res。
 */
@Slf4j
public class demo1_AioFileChannel {
    /**
     * 我发现当我用@Test的模式的时候，访问"helloword/1.txt"就能够访问到，因为@Test认为运行在模块根路径
     * 但是我用main函数的时候，就访问步到，英文main函数运行在项目根路径（
     * 程序运行在哪里不是由程序文件放在哪儿决定的！！！）
     */
    @Test
    public void mainTest() throws IOException {
        try{
            AsynchronousFileChannel s =
                // 参数1，ByteBuffer；
                // 参数2，读取的起始位置；
                // 参数3，附件（一次读不完文件所有内容，那么buffer跟着走，下次继续读取）；
                // 参数4，回调对象 CompletionHandler；
                AsynchronousFileChannel.open(Paths.get("helloword/1.txt"), StandardOpenOption.READ);
            ByteBuffer buffer = ByteBuffer.allocate(16);
            log.debug("begin...");
            s.read(buffer, 0, null, new CompletionHandler<Integer, ByteBuffer>() {
                // 下面这两个方法不是由当前线程执行的，是由另外的线程执行的，因为要异步
                @Override  // read成功
                public void completed(Integer result, ByteBuffer attachment) {
                    log.debug("read completed...{}", result);
                    buffer.flip();
                    debugAll(buffer);
                }

                @Override  // read失败
                public void failed(Throwable exc, ByteBuffer attachment) {
                    log.debug("read failed...");
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }
        log.debug("do other things...");
        System.in.read();  // 这个另外的线程是一个守护线程，如果主线程结束了，即使另外的线程没有完成数据读入buffer的操作也会因为主线程结束而结束
    }


}
