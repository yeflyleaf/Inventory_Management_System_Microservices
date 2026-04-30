package com.example.user.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface DashboardMapper {
    BigDecimal sumTodaySales();
    
    Integer countPendingPurchaseOrders();
    
    Integer countPendingSalesOrders();
    
    List<Map<String, Object>> selectDailyStats(@Param("days") Integer days,
                                               @Param("warehouseId") Long warehouseId,
                                               @Param("category") String category,
                                               @Param("itemName") String itemName);
                                               
    List<Map<String, Object>> selectRecentFlows(@Param("limit") Integer limit);
    
    List<Long> selectDistinctItemIdsRecent(@Param("days") Integer days);
    
    List<Map<String, Object>> selectSalesQtyByCategory();
    
    List<Map<String, Object>> selectSalesAmountByCategory();
}
