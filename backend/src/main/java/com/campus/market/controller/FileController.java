package com.campus.market.controller;

import cn.hutool.core.util.IdUtil;
import com.campus.market.common.BusinessException;
import com.campus.market.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FileController {

    @Value("${campus.upload.path}")
    private String uploadPath;

    @Value("${campus.upload.url-prefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("文件为空");
        }
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf("."));
        }
        String filename = IdUtil.fastSimpleUUID() + ext;
        Path dir = Paths.get(uploadPath).toAbsolutePath();
        try {
            Files.createDirectories(dir);
            Files.copy(file.getInputStream(), dir.resolve(filename), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("文件上传失败");
        }
        Map<String, String> result = new HashMap<>();
        result.put("url", urlPrefix + "/" + filename);
        return Result.success(result);
    }
}
