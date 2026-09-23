package io.chn.redisstudy.common;

import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class FileReader {

    public void ReadFile() {
        // 利用类路径资源加载器获取文件
        ClassPathResource resource = new ClassPathResource("static/text.txt");
        // 判断文件是否存在
        if (!resource.exists()) {
            System.out.println("File not exists");
            return;
        }

        // 包装成缓存流
        try (InputStream stream = resource.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            // 开始读取：每轮循环只调用一次 readLine，避免隔行读取导致漏行
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.err.println("文件读取失败：" + e.getMessage());
            e.printStackTrace();
        }
    }

}
