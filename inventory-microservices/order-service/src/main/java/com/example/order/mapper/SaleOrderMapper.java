package com.example.order.mapper;

import com.example.order.entity.SaleOrder;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SaleOrderMapper {
    int insert(SaleOrder order);
    int update(SaleOrder order);
    int deleteById(Long id);
    SaleOrder selectById(Long id);
    List<SaleOrder> selectAll();
    void deleteAll();
}
