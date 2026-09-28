package io.chn.redisstudy.common;

import jakarta.servlet.http.HttpServletResponse;
import com.alibaba.excel.EasyExcel;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class ExcelExporter {

    public <T> void exportExcel(HttpServletResponse response, List<T> data, Class<T> clazz, String fileName, String sheetName) throws IOException {
        // 设置响应头
        setResponseHeader(response, fileName);
        // 写Excel
        EasExcel.write(response.getOutputStream(), clazz).sheet(sheetName).doWrite(data);
    }

    private void setResponseHeader(HttpServletResponse response, String fileName) {
        // 设置MIME类型
        response.setContentType("application/vnd.ms-excel");

        //设置字符编码，防止中文乱码
        response.setCharacterEncoding("UTF-8");

        // 文件名url编码（处理中文文件名）
        String encodeFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8)
                .replaceAll("\\+", "%20");

        // 设置 Content-Disposition，告诉浏览器下载
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodeFileName + ".xlsx");

        // 暴露响应头，让前端拿到文件名
        response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

    }


}
