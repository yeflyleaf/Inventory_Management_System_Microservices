package com.example.product.service;

import com.example.product.dto.ProductDTO;
import com.example.common.vo.ProductVO;
import java.util.List;

public interface ProductService {
    void addProduct(ProductDTO productDTO);
    void updateProduct(ProductDTO productDTO);
    void deleteProduct(Long id);
    ProductVO getProductById(Long id);
    List<ProductVO> getAllProducts();

    /**
     * 获取导出数据
     */
    java.util.List<com.example.product.vo.ProductExportVO> getExportData();
}
