package p4_future;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultPromise;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ExecutionException;


@Slf4j
public class demo3_Netty_Promise {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 1.创建线程
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        EventLoop eventLoop = group.next();

        // 2.可以主动创建一个“容器”存放结果
        DefaultPromise<Integer> promise = new DefaultPromise<>(eventLoop);

        eventLoop.submit(() -> {
            try {
                log.debug("另开一个线程执行任务");
                Thread.sleep(1000);
                // 另一个线程的执行结果放到promise中
                promise.setSuccess(200);
            } catch (InterruptedException e) {
                promise.setFailure(e);
                e.printStackTrace();
            }

        });

        log.debug("主线程去做其他事情");
        Thread.sleep(1000);

        // 3.主线程做完其他事情，通过promise.get()同步获取结果
        log.debug("返回结果是：" + promise.get());
    }
}
