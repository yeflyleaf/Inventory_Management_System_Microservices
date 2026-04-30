package com.example.product.controller;

import com.example.common.domain.Result;
import com.example.product.service.BarcodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.HashMap;
import java.util.Map;

/**
 * 条形码管理控制器
 * 提供条形码识别功能
 */
@RestController
@RequestMapping("/barcode")
@CrossOrigin
public class BarcodeController {

    @Autowired
    private BarcodeService barcodeService;

    /**
     * 识别图片中的条形码
     * @param file 图片文件
     * @return 识别结果
     */
    @PostMapping("/recognize")
    public Result<Map<String, String>> recognize(@RequestParam("file") MultipartFile file) {
        String code = barcodeService.recognizeBarcodeFromImage(file);
        if (code != null) {
            Map<String, String> data = new HashMap<>();
            data.put("barcode", code);
            return Result.success(data, "识别成功");
        } else {
            return Result.error("未在图片中识别到有效的条形码");
        }
    }
}
