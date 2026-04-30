package com.example.product.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.example.common.domain.Result;
import com.example.product.service.ProductService;
import com.example.common.vo.ProductVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import com.alibaba.excel.EasyExcel;
import com.example.product.vo.ProductExportVO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RefreshScope
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @SentinelResource(value = "getProducts", blockHandler = "handleBlock")
    public Result<List<ProductVO>> getAll() {
        return Result.success(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        return Result.success(productService.getProductById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody com.example.product.dto.ProductDTO product) {
        productService.addProduct(product);
        return Result.success(null, "保存成功");
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody com.example.product.dto.ProductDTO product) {
        product.setId(id);
        productService.updateProduct(product);
        return Result.success(null, "更新成功");
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return Result.success(null, "删除成功");
    }

    public Result<List<ProductVO>> handleBlock(com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return Result.error(429, "当前访问人数过多，请稍后再试");
    }

    /**
     * 导出商品列表为 Excel
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = "products_" + System.currentTimeMillis() + ".xlsx";
        String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);
        
        List<ProductExportVO> data = productService.getExportData();
        EasyExcel.write(response.getOutputStream(), ProductExportVO.class)
                .sheet("商品列表")
                .doWrite(data);
    }
}
