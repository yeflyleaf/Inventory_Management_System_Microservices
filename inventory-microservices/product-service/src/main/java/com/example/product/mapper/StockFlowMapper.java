package com.example.product.mapper;

import com.example.product.entity.StockFlow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface StockFlowMapper {
    int insert(StockFlow stockFlow);
    List<StockFlow> selectAll();
    List<StockFlow> selectByItemId(Long itemId);
    Integer sumStockByItemIdAndWarehouseId(@Param("itemId") Long itemId, @Param("warehouseId") Long warehouseId);
    void deleteByItemId(Long itemId);
    
    @org.apache.ibatis.annotations.MapKey("itemId")
    Map<Long, Map<String, Object>> selectLastMovements();
}
