package p4_future;

import io.netty.channel.EventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Slf4j
public class demo2_Netty_Future {
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

        // 3.主线程做完其他事情，通过future.get()同步获取结果
        log.debug("返回结果是：" + future.get());

    }
}
