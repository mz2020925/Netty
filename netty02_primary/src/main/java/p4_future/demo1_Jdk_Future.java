package p4_future;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;


@Slf4j
public class demo1_Jdk_Future {
    public static void main(String[] args) throws InterruptedException, ExecutionException {
        // 1.线程池
        ExecutorService service = Executors.newFixedThreadPool(2);

        // 2.提交任务，线程池会自动分配线程
        Future<Integer> future = service.submit(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                log.debug("另开一个线程执行任务");
                Thread.sleep(1000);
                return 200;
            }
        });

        log.debug("主线程去做其他事情");
        Thread.sleep(2000);

        // 3.主线程做完其他事情，通过future.get()获取结果
        log.debug("返回结果是：" + future.get());
    }
}
