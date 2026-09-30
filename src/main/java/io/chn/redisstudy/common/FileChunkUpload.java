package io.chn.redisstudy.common;

import cn.hutool.core.io.FileUtil;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@Component
public class FileChunkUpload {

    private long totalChunks;
    private static final long CHUNK_SIZE = 100 * 1024 * 1024;

    // 文件分片
    public void fileChunkUpload(String resource, String uploadDir) throws IOException {
        // 判断文件是否存在
        File file = new File(resource);
        if (!FileUtil.exist(file)) {
            throw new RuntimeException("文件不存在");
        }

        // 创建上传目录
        FileUtil.mkdir(uploadDir);

        // 获取文件大小
        long fileSize = FileUtil.size(file);
        // 计算分片个数
        totalChunks = (fileSize + CHUNK_SIZE - 1) / CHUNK_SIZE;

        // 读取文件内容
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            byte[] buffer = new byte[(int) CHUNK_SIZE];

            for (int i = 0; i < totalChunks; i++) {
                // 定位到第i片的起始位置
                raf.seek(i * CHUNK_SIZE);

                int byteRead = raf.read(buffer);
                if (byteRead == -1) break;

                // 拼接分片文件名称
                String chunkFileName = uploadDir + "chunk_" + i;
                try (FileOutputStream fos = new FileOutputStream(chunkFileName)) {
                    fos.write(buffer, 0, byteRead);
                }
                System.out.println("已写第" + i + "个分片文件：" + chunkFileName + "大小：" + byteRead);
            }

        }

    }

    // 分片文件合并
    public void mergeFileChunks(String uploadDir, String target) throws IOException {

        // 创建目标文件的父目录
        File targetFile = new File(target);
        FileUtil.mkParentDirs(targetFile);

        // 合并
        try (RandomAccessFile raf = new RandomAccessFile(targetFile, "rw");
             // 获取目标文件的文件通道
             FileChannel targetChannel = raf.getChannel()) {
            // 清空目标文件，避免重复合并时残留旧内容
            raf.setLength(0);

            for (int i = 0; i < totalChunks; i++) {
                String chunkFile = uploadDir + "chunk_" + i;
                try (FileChannel chunkChannel = FileChannel.open(Paths.get(chunkFile), StandardOpenOption.READ)) {
                    // 获取分片文件大小
                    long chunkFileSize = chunkChannel.size();
                    // 计算文件写入位置：每片按 CHUNK_SIZE 对齐（最后一片可能不足 CHUNK_SIZE）
                    targetChannel.position((long) i * CHUNK_SIZE);
                    // 写入目标文件
                    long position = 0;
                    while (position < chunkFileSize) {
                        long transferred = chunkChannel.transferTo(position, chunkFileSize - position, targetChannel);
                        if (transferred <= 0) break;
                        position += transferred;
                    }
                    System.out.println("合并第 " + i + " 片，位置：" + (i * CHUNK_SIZE) + "，大小：" + chunkFileSize);
                }
            }
        }

        // 清空分片文件
        for (int i = 0; i < totalChunks; i++) {
            FileUtil.del(uploadDir + "chunk_" + i);
        }
    }
}
