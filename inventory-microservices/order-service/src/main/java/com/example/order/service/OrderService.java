package com.example.order.service;

import com.example.order.entity.SaleOrder;
import com.example.order.feign.StockFeignClient;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private StockFeignClient stockFeignClient;

    @GlobalTransactional(name = "create-order", rollbackFor = Exception.class)
    public void createOrder(SaleOrder order) {
        // 1. 保存订单逻辑 (省略)
        
        // 2. 远程调用扣减库存
        stockFeignClient.adjustStock(1L, -5, "销售出库", 1L, "订单创建扣减库存");
        
        // 模拟异常测试回滚
        // throw new RuntimeException("Testing Seata Rollback");
    }
}
