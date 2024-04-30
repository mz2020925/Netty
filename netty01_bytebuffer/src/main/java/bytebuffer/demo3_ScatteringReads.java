package bytebuffer;

import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static bytebuffer.ByteBufferUtil.debugAll;

public class demo3_ScatteringReads {
    /** 分散读
     * 使用如下方式写入，可以将 channel 存入到多个 buffer 中
     * 效率更高
     */
    @Test
    public void mainTest() {
        try (RandomAccessFile file = new RandomAccessFile("helloword/3parts.txt", "rw")) {
            FileChannel channel = file.getChannel();
            ByteBuffer a = ByteBuffer.allocate(3);
            ByteBuffer b = ByteBuffer.allocate(3);
            ByteBuffer c = ByteBuffer.allocate(5);
            channel.read(new ByteBuffer[]{a, b, c});  // 分别 进三个buffer
            // a.flip();
            // b.flip();
            // c.flip();
            debugAll(a);  // debugAll()没有 出buffer 操作，所以不需要flip()
            debugAll(b);
            debugAll(c);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
