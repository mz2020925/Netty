package bytebuffer;

import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static bytebuffer.ByteBufferUtil.debugAll;

public class demo4_GatheringWrites {
    /**
     * 集中写
     * 使用如下方式写入，可以将多个 buffer 的数据传至 channel
     * 直接将多个buffer 数据传至channel——效率更高，而不是先复制到一个buffer然后统一传至channel，
     */
    @Test
    public void mainTest() {
        try (RandomAccessFile file = new RandomAccessFile("helloword/3parts.txt", "rw")) {
            FileChannel channel = file.getChannel();
            ByteBuffer d = ByteBuffer.allocate(4);
            ByteBuffer e = ByteBuffer.allocate(4);
            channel.position(11);

            d.put(new byte[]{'f', 'o', 'u', 'r'});
            e.put(new byte[]{'f', 'i', 'v', 'e'});

            debugAll(d);
            debugAll(e);
            d.flip();
            e.flip();
            channel.write(new ByteBuffer[]{d, e});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
