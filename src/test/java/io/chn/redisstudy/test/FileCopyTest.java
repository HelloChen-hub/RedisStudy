package io.chn.redisstudy.test;

import io.chn.redisstudy.common.BigFileCopy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileCopyTest {

    BigFileCopy bigFileCopy = new BigFileCopy();
    @Test
    public void fileCopyTest() {
        // 注意：从资源管理器“复制文件地址”粘贴的路径自带双引号，需要去掉，否则引号会成为文件名的一部分导致找不到源文件
        boolean result = bigFileCopy.fileCopy("D:\\VMware_linux\\564d8fab-7819-d0bc-0d39-6f340b169193.vmem", "D:\\FileCopy");
        System.out.println(result ? "文件复制成功" : "文件复制失败");
        assertTrue(result, "文件复制失败，请查看控制台输出的失败原因");
    }

}
