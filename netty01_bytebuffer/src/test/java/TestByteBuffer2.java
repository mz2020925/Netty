import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


@Slf4j
public class TestByteBuffer2 {
    public void test() {
        // 如果想用byteBuffer这套API，需要用到FileChannel--代表数据的读取通道
        // FileChannel可以通过输入输出流获得，也可以通过RandomAccessFile获得
        try (FileChannel channel = new FileInputStream("./netty01_bytebuffer/data.txt").getChannel()) {
            // 准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            while (true){  // 没有内容了
                // 从channel中读取数据，向buffer写入
                int len = channel.read(buffer);
                // log.debug("读取到的字节数{}", len);
                if (len==-1){
                    break;
                }
                // 打印buffer的内容
                buffer.flip();  // 切换至读模式
                // while (buffer.hasRemaining()){  // 是否还有剩余数据
                //     byte b = buffer.get();// 无参get()一次读取一个字节
                //     log.debug("实际字节{}", (char) b);
                // }
                buffer.clear();  // 切换至写模式
            }


        } catch (IOException e) {
            System.out.println(e);
        }
    }

}
