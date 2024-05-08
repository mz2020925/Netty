package p2_eventloop;

import io.netty.channel.DefaultEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class demo2_DefaultEventLoopGroup {
        public static void main(String[] args) {
        // 1.创建循环组，NioEventLoopGroup 常用于处理io事件，普通任务，定时任务
        // DefaultEventLoopGroup不能处理io事件，只能处理普通任务和定时任务
        DefaultEventLoopGroup eventExecutors = new DefaultEventLoopGroup(2);  // 线程数（即EventLoop个数），一个线程一个selector，可以监听多个channel

        // 2.获取下一个EventLoop，这里用的是轮询规则
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());
        System.out.println(eventExecutors.next());

        // 3.执行普通任务。在什么情况下NioEventLoopGroup用来执行普通任务？？？
        // 3.1 例如下面就是新建一个线程去处理一个事件。3.2 当需要把代码的执行权交给另一个线程。
        // .submit()方法和.execute()方法作用一致

        eventExecutors.next().submit(() -> {
            try {
                Thread.sleep(1000);
            }catch (InterruptedException e){
                e.printStackTrace();
            }
            log.debug("普通任务即将执行结束...");
        });

        // 4.执行定时任务
        eventExecutors.next().scheduleAtFixedRate(()->{

            log.debug("定时任务即将执行结束，0s后开始执行（立刻执行），然后每秒执行一次");
        }, 0, 1, TimeUnit.SECONDS);

        log.debug("main函数即将执行结束...");
    }
}
