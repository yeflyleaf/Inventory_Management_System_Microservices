package com.example.order.mapper;

import com.example.order.entity.SaleOrderItem;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface SaleOrderItemMapper {
    int insert(SaleOrderItem item);
    List<SaleOrderItem> selectByOrderId(Long orderId);
    void deleteByOrderId(Long orderId);
    void deleteAll();
}
