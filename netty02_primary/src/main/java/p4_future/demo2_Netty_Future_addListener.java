package p4_future;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;


@Slf4j
public class demo2_Netty_Future_addListener {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        NioEventLoopGroup group = new NioEventLoopGroup(2);
        EventLoop eventLoop = group.next();
        Future<Integer> future = eventLoop.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("另开一个线程执行任务");
                Thread.sleep(1000);
                return 200;
            }
        });

        log.debug("主线程去做其他事情");
        Thread.sleep(2000);

        // 3.主线程通过addListener异步获取结果，就是主线程不去获取结构，让执行前面那个任务的线程去获取结果
        future.addListener(new GenericFutureListener<Future<? super Integer>>() {
            @Override
            public void operationComplete(Future<? super Integer> future) throws Exception {
                log.debug("返回结果是：" + future.get());
            }
        });
    }
}
