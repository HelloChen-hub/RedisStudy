package io.chn.redisstudy.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public interface UploadService {
    void upload(MultipartFile file);
}
