package com.example.product.controller;

import com.example.common.domain.Result;
import com.example.product.dto.StockAdjustDTO;
import com.example.product.service.StockService;
import com.example.product.vo.StockFlowVO;
import com.example.common.vo.ProductVO; 
import com.example.product.vo.StockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/stock")
@CrossOrigin
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping
    public Result<List<StockVO>> getStockSnapshot(@RequestParam(required = false) Long warehouseId) {
        return Result.success(stockService.getStockSnapshot(warehouseId));
    }

    @GetMapping("/snapshot/all")
    public Result<List<StockVO>> getAllStockSnapshot() {
        return Result.success(stockService.getStockSnapshot(null));
    }

    @GetMapping("/flow")
    public Result<List<StockFlowVO>> getAllStockFlows() {
        return Result.success(stockService.getAllStockFlows());
    }

    @PostMapping("/adjust")
    public Result<Void> adjustStock(@RequestBody StockAdjustDTO adjustDTO) {
        stockService.adjustStock(adjustDTO.getItemId(), adjustDTO.getChangeAmount(), 
                             adjustDTO.getChangeType(), 1L, "Manual", null);
        return Result.success(null, "库存调整成功");
    }
    
    // Feign call endpoint
    @PostMapping("/adjust-internal")
    public Result<Void> adjustStockInternal(@RequestParam("itemId") Long itemId,
                                           @RequestParam("changeAmount") Integer changeAmount,
                                           @RequestParam("changeType") String changeType,
                                           @RequestParam("warehouseId") Long warehouseId,
                                           @RequestParam("description") String description) {
        stockService.adjustStock(itemId, changeAmount, changeType, warehouseId, "Order", description);
        return Result.success(null);
    }

    @DeleteMapping("/{itemId}")
    public Result<Void> deleteStock(@PathVariable Long itemId) {
        stockService.deleteStock(itemId);
        return Result.success(null, "库存记录已成功删除");
    }
}
