package fileio;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class demo5_Walk {

    /**
     * 拷贝多级目录
     */
    @Test
    public void mainTest() throws IOException{
        long start = System.nanoTime();
        String source = "e:\\Snipaste-1.16.2-x64";
        String target = "e:\\Snipaste-1.16.2-x64aaa";

        Files.walk(Paths.get(source)).forEach(path -> {  // path是原始目录下的文件夹  或者 文件
            try {
                String targetName = path.toString().replace(source, target);  // 替换字符串，生成目标文件夹路径，或者文件路径
                // 是目录
                if (Files.isDirectory(path)) {
                    Files.createDirectory(Paths.get(targetName));
                }
                // 是普通文件
                else if (Files.isRegularFile(path)) {
                    Files.copy(path, Paths.get(targetName));  // Files.copy(源, 目标)
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        long end = System.nanoTime();
        System.out.println("多级目录拷贝用时："+ (end - start) / 1000_000.0 +"ms");
    }
}

