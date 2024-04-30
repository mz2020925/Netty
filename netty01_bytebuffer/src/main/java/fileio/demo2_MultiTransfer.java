package fileio;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class demo2_MultiTransfer {
    @Test
    public void mainTest() {
        try (
                FileChannel from = new FileInputStream("helloworld/data.txt").getChannel();
                FileChannel to = new FileOutputStream("helloworld/to.txt").getChannel()
        ) {
            // 效率高，底层会利用操作系统的零拷贝进行优化
            long size = from.size();
            // left 变量代表还剩余多少字节
            for (long rest = size; rest > 0; ) {
                System.out.println("position:" + (size - rest) + " left:" + rest);
                rest -= from.transferTo((size - rest), rest, to);  // 每次复制时，源文件的position位置不一样，值是size - rest
                // transferTo返回本次复制结束后，position的位置
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
