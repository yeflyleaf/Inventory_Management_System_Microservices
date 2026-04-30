package com.example.product.service;

import com.example.product.vo.StockVO;
import com.example.product.vo.StockFlowVO;
import java.util.List;

public interface StockService {
    List<StockVO> getStockSnapshot(Long warehouseId);
    List<StockFlowVO> getStockFlows(Long itemId);
    List<StockFlowVO> getAllStockFlows();
    void adjustStock(Long itemId, Integer changeAmount, String changeType, Long warehouseId, String refType, String refId);
    void deleteStock(Long itemId);
}
