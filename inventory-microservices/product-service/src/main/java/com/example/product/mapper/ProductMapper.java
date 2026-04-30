package com.example.product.mapper;

import com.example.product.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductMapper {
    int insert(Product product);
    int update(Product product);
    int deleteById(Long id);
    Product selectById(Long id);
    List<Product> selectAll();
    int countByName(@Param("name") String name, @Param("excludeId") Long excludeId);
    int countBySku(@Param("sku") String sku, @Param("excludeId") Long excludeId);
}
