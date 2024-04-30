package fileio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class demo1_FileChannelDemo {
    @Test
    public void mainTest() {
        String FROM = "helloword/data.txt";
        String TO = "helloword/to.txt";
        long start = System.nanoTime();
        // 下面是try-with-resource用法
        // 当try中代码执行结束后（正常结束或者异常结束），都会调用try()括号中对象的close()方法来关闭资源
        try (
                FileChannel from = new FileInputStream(FROM).getChannel();
                FileChannel to = new FileOutputStream(TO).getChannel()
        ) {
            // 在Java中，只要是带着transfer的，底层都是用操作系统零拷贝实现优化
            from.transferTo(0, from.size(), to);  // 把from文件中position从[0, from.size())的内容复制到to文件中
            // transferTo(0, from.size(), to);方法复制文件时，文件大小不能超过2G，对于超过2G的需要多次传输
        } catch (IOException e) {
            e.printStackTrace();
        }
        long end = System.nanoTime();
        System.out.println("transferTo 用时：" + (end - start) / 1000_000.0 + "ms");
    }
}
