package io.chn.redisstudy.config.logConfig;

import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LogFileWriteConfig {
    private static final String Log_Dir = "D:/logs/";
    private static final DateTimeFormatter Date_Format = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter Time_Format = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Object lock = new Object();

    // 日志写入操作
    public void writeLog(String level, String message) {
        // 生成日志文件名
        String dateName = LocalDateTime.now().format(Date_Format);
        String fileName = Log_Dir + dateName + ".log";
        // 创建文件对象
        File file = new File(Log_Dir);
        // 判断文件夹是否存在
        if (!file.exists()) {
            file.mkdirs();
        }

        //System.out.println("绝对路径：" + file.getAbsolutePath());

        // 拼装成日志行
        String line = LocalDateTime.now().format(Time_Format) + " [" + level + "] " + message;

        // 写入日志文件(加互斥锁，让多线程情况下串行写日志）
        synchronized (lock) {
            try (BufferedWriter bufferedWriter =
                    new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(fileName, true)
                    , StandardCharsets.UTF_8))) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
