package fileio;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

public class demo4_WalkFileTree {
    /**
     * 遍历指定目录下所有文件夹和文件。下面的代码时 访问者设计模式 的应用
     */
    @Test
    public void findAllDirAndFiles() throws IOException {
        Path path = Paths.get("C:\\Program Files\\Java\\jdk1.8.0_361");
        AtomicInteger dirCount = new AtomicInteger();
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            /**
             * preVisitDirectory()表示遍历过程中 进入文件夹之前 需要执行的操作
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println(dir);
                dirCount.incrementAndGet();  // 增加
                return super.preVisitDirectory(dir, attrs);
            }

            /**
             * visitFile()表示遍历过程中 遍历到文件 需要执行的操作
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                fileCount.incrementAndGet();  // 增加
                return super.visitFile(file, attrs);
            }
        });
        System.out.println(dirCount); // 133
        System.out.println(fileCount); // 1479
    }

    /**
     * 统计问价 .jar 的数目
     */
    @Test
    public void findFiles() throws IOException {
        Path path = Paths.get("C:\\Program Files\\Java\\jdk1.8.0_361");
        AtomicInteger fileCount = new AtomicInteger();
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            /**
             * visitFile()表示遍历过程中 遍历到文件 需要执行的操作
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toFile().getName().endsWith(".jar")) {
                    fileCount.incrementAndGet();
                }
                return super.visitFile(file, attrs);
            }
        });
        System.out.println(fileCount); // 724
    }

    /**
     * 删除操作需要谨慎，因为程序删除相当于永久删除，需要先删除文件夹中文件，才能删除文件夹。
     */
    public void deleteDirAndFiles() throws IOException {
        Path path = Paths.get("e:\\a");
        Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
            /**
             * preVisitDirectory()表示遍历过程中 进入文件夹之前 需要执行的操作
             */
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("进入===> " + dir);
                return super.preVisitDirectory(dir, attrs);
            }

            /**
             * visitFile()表示遍历过程中 遍历到文件 需要执行的操作
             */
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                System.out.println(file);
                Files.delete(file);
                return super.visitFile(file, attrs);
            }

            /**
             * preVisitDirectory()表示遍历过程中 退出文件夹之后 需要执行的操作
             */
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                System.out.println("退出<=== " + dir);
                Files.delete(dir);
                return super.postVisitDirectory(dir, exc);
            }
        });
    }
}
