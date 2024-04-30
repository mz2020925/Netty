import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class TestByteBuffer {
    public static void main(String[] args) {
        // 如果想用byteBuffer这套API，需要用到FileChannel--代表数据的读取通道
        // FileChannel可以通过输入输出流获得，也可以通过RandomAccessFile获得
        try (FileChannel channel = new FileInputStream("./netty01_bytebuffer/data.txt").getChannel()) {
            // 准备缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(10);
            // 从channel中读取数据，向buffer写入
            channel.read(buffer);
            // 打印buffer的内容
            buffer.flip();  // 切换至读模式
            while (buffer.hasRemaining()){  // 是否还有剩余数据
                byte b = buffer.get();// 无参get()一次读取一个字节
                System.out.println((char) b);
            }

        } catch (IOException e) {
            System.out.println(e);
        }
    }

}
