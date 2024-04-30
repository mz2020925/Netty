package bytebuffer;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;

import static bytebuffer.ByteBufferUtil.debugAll;

public class demo2_StringTransferByteBuffer {

    @Test
    public void stringAndByteBuffer() {
        // 字符串转buffer
        ByteBuffer buffer1 = StandardCharsets.UTF_8.encode("hello");  // 没有给buffer1分配容量，默认时"hello"对应的容量，也就是5个字节
        // ByteBuffer buffer2 = Charset.forName("utf-8").encode("hello");
        // ByteBuffer buffer3 = ByteBuffer.wrap("hello".getBytes());  // 没有指定编码方式，那就是按照系统编码
        // ByteBuffer buffer4 = ByteBuffer.allocate(16);
        // buffer4.put("hello".getBytes());

        debugAll(buffer1);
        // debugAll(buffer2);

        // buffer转字符串
        buffer1.flip();  // 必须移动position后，出buffer才有意义
        CharBuffer buffer = StandardCharsets.UTF_8.decode(buffer1);
        // System.out.println(buffer.getClass());
        System.out.println(buffer.toString());
    }
    /*
    * UTF-8编码对一个汉字是3个字节，对一个字符（字母、数字、标点符号）是1个字节
    *
    * */
}
