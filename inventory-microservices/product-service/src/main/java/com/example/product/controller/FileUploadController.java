package com.example.product.controller;

import com.example.common.domain.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文件上传控制器
 * 处理文件上传请求
 */
@RestController
@RequestMapping("/upload")
@CrossOrigin
public class FileUploadController {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    /**
     * 上传商品图片
     */
    @PostMapping("/product-image")
    public Result<Map<String, String>> uploadProductImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的图片");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        if (file.getSize() > 20 * 1024 * 1024) {
            return Result.error("图片大小不能超过20MB");
        }

        try {
            String tempPath = uploadPath + "/temp";
            Path uploadDir = Paths.get(tempPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            String imageUrl = "/api/uploads/temp/" + newFilename;
            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            data.put("filename", newFilename);

            return Result.success(data, "图片上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("图片上传失败: " + e.getMessage());
        }
    }

    /**
     * 删除商品图片
     */
    @DeleteMapping("/product-image")
    public Result<Void> deleteProductImage(@RequestParam("filename") String filename) {
        try {
            Path productFilePath = Paths.get(uploadPath, "products", filename);
            Path tempFilePath = Paths.get(uploadPath, "temp", filename);
            
            boolean deleted = false;
            
            if (Files.exists(tempFilePath)) {
                Files.delete(tempFilePath);
                deleted = true;
            } else if (Files.exists(productFilePath)) {
                Files.delete(productFilePath);
                deleted = true;
            }
            
            if (deleted) {
                return Result.success(null, "图片删除成功");
            } else {
                return Result.error("图片不存在");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("图片删除失败: " + e.getMessage());
        }
    }

    /**
     * 上传用户头像
     */
    @PostMapping("/avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("请选择要上传的头像");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error("只能上传图片文件");
        }

        if (file.getSize() > 20 * 1024 * 1024) {
            return Result.error("头像大小不能超过20MB");
        }

        try {
            String avatarPath = uploadPath + "/avatars";
            Path uploadDir = Paths.get(avatarPath);
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            Path filePath = uploadDir.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            String imageUrl = "/api/uploads/avatars/" + newFilename;
            Map<String, String> data = new HashMap<>();
            data.put("url", imageUrl);
            data.put("filename", newFilename);

            return Result.success(data, "头像上传成功");
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error("头像上传失败: " + e.getMessage());
        }
    }
}
