package com.example.order.controller;

import com.example.common.annotation.Log;
import com.example.common.domain.Result;
import com.example.order.dto.SaleOrderDTO;
import com.example.order.service.SaleOrderService;
import com.example.order.vo.SaleOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RefreshScope
@RequestMapping("/sale-orders")
@CrossOrigin
public class SaleOrderController {

    @Autowired
    private SaleOrderService saleOrderService;

    @GetMapping
    public Result<List<SaleOrderVO>> getAll() {
        return Result.success(saleOrderService.getAllOrders(), "获取销售订单列表成功");
    }

    @GetMapping("/{id}")
    public Result<SaleOrderVO> getById(@PathVariable Long id) {
        return Result.success(saleOrderService.getOrderById(id), "获取销售订单详情成功");
    }

    @PostMapping
    @Log(module = "销售管理", action = "创建订单", description = "创建销售订单")
    public Result<Void> create(@RequestBody SaleOrderDTO orderDTO, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) userId = 1L; // Fallback
        saleOrderService.createOrder(orderDTO, userId);
        return Result.success(null, "创建销售订单成功");
    }

    @PostMapping("/{id}/ship")
    @Log(module = "销售管理", action = "订单发货", description = "销售订单发货(出库)")
    public Result<Void> ship(@PathVariable Long id) {
        saleOrderService.shipOrder(id);
        return Result.success(null, "销售订单发货成功");
    }

    @DeleteMapping
    @Log(module = "销售管理", action = "清空订单", description = "清空所有销售订单")
    public Result<Void> deleteAll() {
        saleOrderService.deleteAllOrders();
        return Result.success(null, "清空销售订单成功");
    }

    @DeleteMapping("/{id}")
    @Log(module = "销售管理", action = "删除订单", description = "删除销售订单")
    public Result<Void> delete(@PathVariable Long id) {
        saleOrderService.deleteOrder(id);
        return Result.success(null, "删除销售订单成功");
    }
}
