package io.chn.redisstudy.common;

import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class BigFileCopy {

    //TODO 首先判断文件是否存在
    public boolean fileCopy(String source, String target) {

        // 判断文件是否存在
        File file = new File(source);
        if (!file.exists()) {
            System.err.println("源文件不存在: " + file.getAbsolutePath());
            return false;
        }

        // 目标路径如果是已存在的目录，则复制到该目录下，并保持与源文件相同的文件名
        Path targetPath = Path.of(target);
        if (Files.isDirectory(targetPath)) {
            targetPath = targetPath.resolve(file.getName());
        }

        long position = 0;
        // 打开文件通道
        try (FileChannel inputChannel = FileChannel.open(Path.of(source), StandardOpenOption.READ);
        FileChannel outPutChannel = FileChannel.open(targetPath, StandardOpenOption.CREATE, StandardOpenOption.WRITE))
        {
            while (position < inputChannel.size()) {
                long transferred = inputChannel.transferTo(position, inputChannel.size()-position, outPutChannel);
                if (transferred <= 0) {
                    // 防御性保护：无法继续推进时跳出，避免死循环
                    System.err.println("文件传输中断，已传输 " + position + " 字节");
                    return false;
                }
                position += transferred;
            }
            return true;
        } catch (IOException e) {
            // 打印失败原因，便于排查（返回值 false 表示复制失败）
            System.err.println("文件复制失败: " + e.getMessage());
            return false;
        }
    }
}
