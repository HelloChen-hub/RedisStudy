package io.chn.redisstudy.service.impl;

import io.chn.redisstudy.common.FileLoad;
import io.chn.redisstudy.service.UploadService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class UploadServiceImpl implements UploadService {
    @Resource
    private FileLoad fileLoad;

    private static final String UPLOAD_DIR = "D:/uploads/avatar";

    @Override
    public void upload(MultipartFile file) {
        try {
            fileLoad.fileLoad(file, UPLOAD_DIR);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
