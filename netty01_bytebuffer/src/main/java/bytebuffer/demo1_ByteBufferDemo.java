package bytebuffer;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

@Slf4j
public class demo1_ByteBufferDemo {
    @Test
    public void mainTest() {
        // System.out.println(System.getProperty("user.dir"));
        // 如果想用ByteBuffer这套API，需要用到FileChannel--代表数据的读取通道
        // FileChannel可以通过输入输出流获得，也可以通过RandomAccessFile获得
        try (RandomAccessFile file = new RandomAccessFile("helloword/data.txt", "rw")) {
            FileChannel channel = file.getChannel();
            // 准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);  // 缓存区容量 10Byte
            do {
                // 进buffer
                int len = channel.read(buffer);  // len是position由于数据进来，移动的格子数
                log.debug("读到字节数：{}", len);
                if (len == -1) {
                    break;
                }
                // 切换 出buffer 模式
                buffer.flip();  // flip是把position移动到第一个 可出buffer 的格子
                while (buffer.hasRemaining()) {  //
                    byte b = buffer.get();
                    log.debug("{}", (char) b);
                }
                // 切换 进buffer 模式，从头开始 进buffer
                buffer.clear();
                // System.out.println("clear之后：" + (char) buffer.get());
                // buffer.clear();切换到 进buffer 模式是把position移动到了开头位置，并不是说这时不能 出buffer了；而且此时buffer中的数据也并没有被清空，之后的 进buffer 的时候会从position指向的各自开始覆盖。所以这里的buffer.get()获得了 1

                // buffer.compact();
                // System.out.println("clear之后：" + (char) buffer.get());
                /// buffer.compact()同样是切换到 进buffer 模式，但这个会把 出buffer 的格子数据清空，把剩余数据移动到开头，然后position指向第一个空格子。所以这里的buffer.get()获得了
            } while (true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}