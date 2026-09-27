package io.chn.redisstudy.controller;

import io.chn.redisstudy.service.UploadService;
import io.chn.redisstudy.utils.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Resource
    private UploadService uploadService;

    @PostMapping
    public Result<Void> upload(@RequestParam("avatar") MultipartFile file) {
        uploadService.upload(file);
        return Result.success();
    }
}
