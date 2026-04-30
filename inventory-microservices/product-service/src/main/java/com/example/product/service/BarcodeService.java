package com.example.product.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 条形码服务接口
 */
public interface BarcodeService {
    /**
     * 从图片中识别条形码
     * @param file 图片文件
     * @return 识别出的内容
     */
    String recognizeBarcodeFromImage(MultipartFile file);
}
