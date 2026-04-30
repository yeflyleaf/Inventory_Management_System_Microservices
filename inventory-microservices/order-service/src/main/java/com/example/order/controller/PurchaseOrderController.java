package com.example.order.controller;

import com.example.common.annotation.Log;
import com.example.common.domain.Result;
import com.example.order.dto.PurchaseOrderDTO;
import com.example.order.service.PurchaseOrderService;
import com.example.order.vo.PurchaseOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import com.alibaba.excel.EasyExcel;
import com.example.order.vo.PurchaseOrderExportVO;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 采购订单管理控制器
 * 提供采购订单的增删改查及状态流转功能
 */
@RestController
@RefreshScope
@RequestMapping("/purchase-orders")
@CrossOrigin
public class PurchaseOrderController {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    /**
     * 获取所有采购订单
     * @return 采购订单列表
     */
    @GetMapping
    @SentinelResource(value = "getAllPurchaseOrders", blockHandler = "handleBlock")
    public Result<List<PurchaseOrderVO>> getAll() {
        List<PurchaseOrderVO> orders = purchaseOrderService.getAllOrders();
        return Result.success(orders, "获取采购订单列表成功");
    }

    public Result<List<PurchaseOrderVO>> handleBlock(BlockException ex) {
        return Result.error(429, "采购查询繁忙，请稍后再试");
    }

    /**
     * 获取采购订单详情
     * @param id 订单ID
     * @return 订单详情
     */
    @GetMapping("/{id}")
    public Result<PurchaseOrderVO> getById(@PathVariable Long id) {
        PurchaseOrderVO order = purchaseOrderService.getOrderById(id);
        return Result.success(order, "获取采购订单详情成功");
    }

    /**
     * 创建采购订单
     * @param orderDTO 订单信息
     * @param request HTTP请求
     * @return 成功信息
     */
    @PostMapping
    @Log(module = "采购管理", action = "创建订单", description = "创建采购订单")
    public Result<Void> create(@RequestBody PurchaseOrderDTO orderDTO, HttpServletRequest request) {
        // In a microservices environment, userId can be extracted from a header or attribute set by gateway/filter
        // To maintain original logic, we use the attribute as in the monolith.
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            // Fallback for testing: if no userId, use a default or throw exception
            // userId = 1L; 
            throw new RuntimeException("User not authenticated");
        }
        purchaseOrderService.createOrder(orderDTO, userId);
        return Result.success(null, "创建采购订单成功");
    }

    /**
     * 完成采购订单(入库)
     * @param id 订单ID
     * @return 成功信息
     */
    @PostMapping("/{id}/finish")
    @Log(module = "采购管理", action = "完成订单", description = "完成采购订单(入库)")
    public Result<Void> finish(@PathVariable Long id) {
        purchaseOrderService.finishOrder(id);
        return Result.success(null, "完成采购订单成功");
    }

    /**
     * 清空所有采购订单
     * @return 成功信息
     */
    @DeleteMapping
    @Log(module = "采购管理", action = "清空订单", description = "清空所有采购订单")
    public Result<Void> deleteAll() {
        purchaseOrderService.deleteAllOrders();
        return Result.success(null, "清空采购订单成功");
    }

    /**
     * 删除采购订单
     * @param id 订单ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Log(module = "采购管理", action = "删除订单", description = "删除采购订单")
    public Result<Void> delete(@PathVariable Long id) {
        purchaseOrderService.deleteOrder(id);
        return Result.success(null, "删除采购订单成功");
    }

    /**
     * 导出采购订单明细为 Excel
     * @param response HTTP 响应
     * @throws IOException IO 异常
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = "purchase_orders_" + System.currentTimeMillis() + ".xlsx";
        // 防止中文乱码
        String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedFileName);
        
        List<PurchaseOrderExportVO> data = purchaseOrderService.getExportData();
        EasyExcel.write(response.getOutputStream(), PurchaseOrderExportVO.class)
                .sheet("采购订单")
                .doWrite(data);
    }
}
