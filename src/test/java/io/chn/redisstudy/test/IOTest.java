package io.chn.redisstudy.test;

import io.chn.redisstudy.common.FileReader;
import io.chn.redisstudy.entity.UserIO;
import org.junit.jupiter.api.Test;

import java.io.*;

public class IOTest {

    FileReader fileReader = new FileReader();

    @Test
    public void testFileReader() {
        fileReader.ReadFile();
    }

    @Test
    public void testIOUser() {
        String path = "user.ser";
        UserIO io = new UserIO("chn", 19, "123456");

        // 序列化
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path))) {
            // 将文件写入指定文件
            oos.writeObject(io);
            System.out.println("Serialized data is saved in " + path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // 反序列化，从文件中读取数据
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path))) {
            UserIO userIO = (UserIO) ois.readObject();
            System.out.println("Deserialized UserIO: " + userIO.getName() + ", " + userIO.getAge() + ", " + userIO.getPassword());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }


    // TODO selectKey除了具有channel的引用，还有什么，比如channel绑定的事件的标识等等

}