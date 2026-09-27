package io.chn.redisstudy.common;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Component
public class FileLoad {
    public void fileLoad(MultipartFile file, String target) throws IOException {
        // 判断文件是否存在
        if (file.isEmpty()) {
            throw new RuntimeException("文件为空");
        }

        // 判断文件大小是否合规
        if (file.getSize() > 1024 * 1024 * 10) {
            throw new RuntimeException("文件大小超出限制");
        }

        // 获取文件后缀名
        String etc = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            etc += originalName.substring(originalName.lastIndexOf("."));
            etc = etc.toLowerCase();
        }

        // 为文件生成唯一的文件名(实际业务不会去校验文件名称，而是为每一个文件生成唯一的文件名称）
        String fileName = UUID.randomUUID().toString().replaceAll("-", "") + etc;

        // 创建目标目录对象
        String datePath = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path targetPath = Paths.get(target, datePath);
        // 创建多级目录
        Files.createDirectories(targetPath);
        // 将目标路径和文件名拼接成完整的文件路径
        Path uploadPath = targetPath.resolve(fileName);

        // 将文件保存在指定磁盘位置
        try {
            file.transferTo(uploadPath.toFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
