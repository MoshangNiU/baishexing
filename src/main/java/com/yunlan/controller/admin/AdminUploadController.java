package com.yunlan.controller.admin;

import com.yunlan.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/admin-api")
@Api(tags = "管理端 - 文件上传")
public class AdminUploadController {

    @Value("${upload.path:./uploads}")
    private String uploadPath;

    @Value("${upload.url-prefix:/uploads}")
    private String urlPrefix;

    @PostMapping("/upload")
    @ApiOperation("上传图片")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            ext = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        String fileName = UUID.randomUUID().toString().replace("-", "") + ext;
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            return Result.error("上传失败: " + e.getMessage());
        }
        String url = urlPrefix + "/" + fileName;
        Map<String, Object> data = new HashMap<>();
        data.put("url", url);
        data.put("fileName", fileName);
        return Result.success(data);
    }
}
