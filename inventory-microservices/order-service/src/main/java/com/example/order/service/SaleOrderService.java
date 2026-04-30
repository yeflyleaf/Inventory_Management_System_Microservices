package com.example.order.service;

import com.example.order.dto.SaleOrderDTO;
import com.example.order.vo.SaleOrderVO;
import java.util.List;

public interface SaleOrderService {
    void createOrder(SaleOrderDTO orderDTO, Long userId);
    void shipOrder(Long orderId); 
    List<SaleOrderVO> getAllOrders();
    SaleOrderVO getOrderById(Long id);
    void deleteAllOrders();
    void deleteOrder(Long id);
}
