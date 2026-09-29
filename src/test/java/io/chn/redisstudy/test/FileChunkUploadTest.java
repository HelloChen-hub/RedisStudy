package io.chn.redisstudy.test;

import io.chn.redisstudy.common.FileChunkUpload;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;


public class FileChunkUploadTest {
    @Test
    public void testFileChunkUpload() throws IOException {

        String resource = "D:\\VMware_linux\\564d8fab-7819-d0bc-0d39-6f340b169193.vmem";
        // 目标文件必须是完整文件路径，父目录不存在时会自动创建
        String target = "D:\\upload\\";
        String uploadDir = "D:\\ChunkUpload\\";
        FileChunkUpload fileChunkUpload = new FileChunkUpload();
        fileChunkUpload.fileChunkUpload(resource, uploadDir);
        fileChunkUpload.mergeFileChunks(uploadDir, target);
        System.out.println("文件分片上传成功！");
    }
}
