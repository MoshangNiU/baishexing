package com.yunlan.controller.consumer;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.PutObjectResult;
import com.yunlan.common.Result;
import com.yunlan.config.OssConfig;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/customer/consumer/oss")
@Api(tags = "文件上传模块")
public class OssController {

    @Resource
    private OssConfig ossConfig;

    @PostMapping("/upload")
    @ApiOperation("上传文件到OSS")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }
        try {
            String originalFilename = file.getOriginalFilename();
            String ext = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                ext = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String objectName = "uploads/" + UUID.randomUUID().toString().replace("-", "") + ext;

            OSS ossClient = new OSSClientBuilder().build(
                    ossConfig.getEndpoint(),
                    ossConfig.getAccessKeyId(),
                    ossConfig.getAccessKeySecret()
            );

            try (InputStream inputStream = file.getInputStream()) {
                PutObjectResult result = ossClient.putObject(ossConfig.getBucketName(), objectName, inputStream);
                String url = (ossConfig.getDomain() != null && !ossConfig.getDomain().isEmpty()
                        ? ossConfig.getDomain()
                        : "https://" + ossConfig.getBucketName() + "." + ossConfig.getEndpoint())
                        + "/" + objectName;

                Map<String, Object> data = new HashMap<>();
                data.put("url", url);
                data.put("objectName", objectName);
                return Result.success(data);
            } finally {
                ossClient.shutdown();
            }
        } catch (Exception e) {
            return Result.error("上传失败: " + e.getMessage());
        }
    }
}
