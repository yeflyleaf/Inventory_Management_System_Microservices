package com.example.order.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order.mapper.SaleOrderItemMapper;
import com.example.order.mapper.SaleOrderMapper;
import com.example.order.dto.SaleOrderDTO;
import com.example.order.dto.SaleOrderItemDTO;
import com.example.order.entity.SaleOrder;
import com.example.order.entity.SaleOrderItem;
import com.example.order.service.SaleOrderService;
import com.example.order.vo.SaleOrderItemVO;
import com.example.order.vo.SaleOrderVO;
import com.example.order.feign.RemoteProductService;
import com.example.order.feign.RemoteUserService;
import com.example.order.feign.StockFeignClient;
import com.example.common.vo.CustomerVO;
import com.example.common.vo.ProductVO;

import io.seata.spring.annotation.GlobalTransactional;

/**
 * 销售订单服务实现类
 */
@Service
public class SaleOrderServiceImpl implements SaleOrderService {

    @Autowired
    private SaleOrderMapper saleOrderMapper;

    @Autowired
    private SaleOrderItemMapper saleOrderItemMapper;

    @Autowired
    private RemoteProductService remoteProductService;

    @Autowired
    private RemoteUserService remoteUserService;

    @Autowired
    private StockFeignClient stockFeignClient;

    /**
     * 创建销售订单
     */
    @Override
    @GlobalTransactional(name = "create-sale-order", rollbackFor = Exception.class)
    @CacheEvict(value = "sale_orders", allEntries = true)
    public void createOrder(SaleOrderDTO orderDTO, Long userId) {
        SaleOrder order = new SaleOrder();
        String prefix = remoteUserService.getSettingValue("order_prefix_sales", "SO").getData();
        order.setOrderNo(prefix + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase());
        order.setCustomerId(orderDTO.getCustomerId());
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedBy(userId);

        BigDecimal total = BigDecimal.ZERO;
        for (SaleOrderItemDTO itemDTO : orderDTO.getItems()) {
            if (itemDTO.getQty() == null || itemDTO.getQty() <= 0) {
                throw new RuntimeException("商品数量必须为正整数");
            }
            BigDecimal subtotal = itemDTO.getSalePrice().multiply(BigDecimal.valueOf(itemDTO.getQty()));
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);

        saleOrderMapper.insert(order);

        for (SaleOrderItemDTO itemDTO : orderDTO.getItems()) {
            SaleOrderItem item = new SaleOrderItem();
            item.setSalesOrderId(order.getId());
            item.setItemId(itemDTO.getItemId());
            item.setQty(itemDTO.getQty());
            item.setSalePrice(itemDTO.getSalePrice());
            item.setSubtotal(itemDTO.getSalePrice().multiply(BigDecimal.valueOf(itemDTO.getQty())));
            saleOrderItemMapper.insert(item);
        }
    }

    /**
     * 销售订单发货 (出库操作)
     */
    @Override
    @GlobalTransactional(name = "ship-sale-order", rollbackFor = Exception.class)
    @CacheEvict(value = "sale_orders", allEntries = true)
    public void shipOrder(Long orderId) {
        SaleOrder order = saleOrderMapper.selectById(orderId);
        if (order == null || !"CREATED".equals(order.getStatus())) {
            throw new RuntimeException("Order not found or already shipped");
        }

        order.setStatus("SHIPPED");
        saleOrderMapper.update(order);

        List<SaleOrderItem> items = saleOrderItemMapper.selectByOrderId(orderId);
        for (SaleOrderItem item : items) {
            // Outbound: Negative quantity
            stockFeignClient.adjustStock(item.getItemId(), -item.getQty(), "销售出库", 1L, "销售单: " + order.getOrderNo());
        }
    }

    @Override
    @Cacheable(value = "sale_orders", key = "'all'")
    public List<SaleOrderVO> getAllOrders() {
        List<SaleOrder> orders = saleOrderMapper.selectAll();
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "sale_orders", key = "'order:' + #id")
    public SaleOrderVO getOrderById(Long id) {
        SaleOrder order = saleOrderMapper.selectById(id);
        return convertToVO(order);
    }

    private SaleOrderVO convertToVO(SaleOrder order) {
        if (order == null)
            return null;
        SaleOrderVO vo = new SaleOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setCustomerId(order.getCustomerId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setCreatedBy(order.getCreatedBy());

        CustomerVO customer = remoteProductService.getCustomerById(order.getCustomerId()).getData();
        if (customer != null) {
            vo.setCustomerName(customer.getName());
        }

        List<SaleOrderItem> items = saleOrderItemMapper.selectByOrderId(order.getId());
        vo.setItems(items.stream().map(item -> {
            SaleOrderItemVO itemVO = new SaleOrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setItemId(item.getItemId());
            ProductVO product = remoteProductService.getProductById(item.getItemId()).getData();
            if (product != null) {
                itemVO.setItemName(product.getName());
                itemVO.setItemSku(product.getSku());
            }
            itemVO.setQty(item.getQty());
            itemVO.setSalePrice(item.getSalePrice());
            itemVO.setSubtotal(item.getSubtotal());
            return itemVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    @Override
    @Transactional
    @CacheEvict(value = "sale_orders", allEntries = true)
    public void deleteAllOrders() {
        saleOrderItemMapper.deleteAll();
        saleOrderMapper.deleteAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = "sale_orders", allEntries = true)
    public void deleteOrder(Long id) {
        saleOrderItemMapper.deleteByOrderId(id);
        saleOrderMapper.deleteById(id);
    }
}
