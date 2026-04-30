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
import io.seata.spring.annotation.GlobalTransactional;

import com.example.order.mapper.PurchaseOrderItemMapper;
import com.example.order.mapper.PurchaseOrderMapper;
import com.example.order.dto.PurchaseOrderDTO;
import com.example.order.dto.PurchaseOrderItemDTO;
import com.example.order.entity.PurchaseOrder;
import com.example.order.entity.PurchaseOrderItem;
import com.example.order.service.PurchaseOrderService;
import com.example.order.feign.RemoteProductService;
import com.example.order.feign.RemoteUserService;
import com.example.order.feign.StockFeignClient;
import com.example.order.vo.PurchaseOrderItemVO;
import com.example.order.vo.PurchaseOrderExportVO;
import com.example.order.vo.PurchaseOrderVO;
import com.example.common.vo.ProductVO;
import com.example.common.vo.SupplierVO;

/**
 * 采购订单服务实现类
 */
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {

    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private PurchaseOrderItemMapper purchaseOrderItemMapper;

    @Autowired
    private RemoteProductService remoteProductService;

    @Autowired
    private StockFeignClient stockFeignClient;

    @Autowired
    private RemoteUserService remoteUserService;

    @Override
    public List<PurchaseOrderExportVO> getExportData() {
        List<PurchaseOrder> orders = purchaseOrderMapper.selectAll();
        return orders.stream().map(order -> {
            PurchaseOrderExportVO vo = new PurchaseOrderExportVO();
            vo.setId(order.getId());
            vo.setOrderNo(order.getOrderNo());
            vo.setSupplierId(order.getSupplierId());
            vo.setTotalAmount(order.getTotalAmount());
            vo.setStatus(order.getStatus());
            vo.setCreatedAt(order.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());
    }

    /**
     * 创建采购订单
     */
    @Override
    @GlobalTransactional(name = "create-purchase-order", rollbackFor = Exception.class)
    @Transactional
    @CacheEvict(value = "purchase_orders", allEntries = true)
    public void createOrder(PurchaseOrderDTO orderDTO, Long userId) {
        PurchaseOrder order = new PurchaseOrder();
        String prefix = remoteUserService.getSettingValue("order_prefix_purchase", "PO").getData();
        order.setOrderNo(prefix + System.currentTimeMillis() + java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase()); 
        order.setSupplierId(orderDTO.getSupplierId());
        order.setStatus("CREATED");
        order.setCreatedAt(LocalDateTime.now());
        order.setCreatedBy(userId);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderItemDTO itemDTO : orderDTO.getItems()) {
            if (itemDTO.getQty() == null || itemDTO.getQty() <= 0) {
                throw new RuntimeException("商品数量必须为正整数");
            }
            BigDecimal subtotal = itemDTO.getCostPrice().multiply(BigDecimal.valueOf(itemDTO.getQty()));
            total = total.add(subtotal);
        }
        order.setTotalAmount(total);

        purchaseOrderMapper.insert(order);

        for (PurchaseOrderItemDTO itemDTO : orderDTO.getItems()) {
            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrderId(order.getId());
            item.setItemId(itemDTO.getItemId());
            item.setQty(itemDTO.getQty());
            item.setCostPrice(itemDTO.getCostPrice());
            item.setSubtotal(itemDTO.getCostPrice().multiply(BigDecimal.valueOf(itemDTO.getQty())));
            purchaseOrderItemMapper.insert(item);
        }
    }

    /**
     * 完成采购订单 (入库操作)
     */
    @Override
    @io.seata.spring.annotation.GlobalTransactional(name = "finish-purchase-order", rollbackFor = Exception.class)
    @CacheEvict(value = "purchase_orders", allEntries = true)
    public void finishOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderMapper.selectById(orderId);
        if (order == null || !"CREATED".equals(order.getStatus())) {
            throw new RuntimeException("Order not found or already finished");
        }

        order.setStatus("FINISHED");
        purchaseOrderMapper.update(order);

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectByOrderId(orderId);
        for (PurchaseOrderItem item : items) {
            // Inbound: Positive quantity
            stockFeignClient.adjustStock(item.getItemId(), item.getQty(), "采购入库", 1L, "采购订单: " + order.getOrderNo());
        }
    }


    @Override
    @Cacheable(value = "purchase_orders", key = "'all'")
    public List<PurchaseOrderVO> getAllOrders() {
        List<PurchaseOrder> orders = purchaseOrderMapper.selectAll();
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "purchase_orders", key = "'order:' + #id")
    public PurchaseOrderVO getOrderById(Long id) {
        PurchaseOrder order = purchaseOrderMapper.selectById(id);
        return convertToVO(order);
    }

    private PurchaseOrderVO convertToVO(PurchaseOrder order) {
        if (order == null)
            return null;
        PurchaseOrderVO vo = new PurchaseOrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setSupplierId(order.getSupplierId());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setStatus(order.getStatus());
        vo.setCreatedAt(order.getCreatedAt());
        vo.setCreatedBy(order.getCreatedBy());

        SupplierVO supplier = remoteProductService.getSupplierById(order.getSupplierId()).getData();
        if (supplier != null) {
            vo.setSupplierName(supplier.getName());
        }

        List<PurchaseOrderItem> items = purchaseOrderItemMapper.selectByOrderId(order.getId());
        vo.setItems(items.stream().map(item -> {
            PurchaseOrderItemVO itemVO = new PurchaseOrderItemVO();
            itemVO.setId(item.getId());
            itemVO.setItemId(item.getItemId());
            ProductVO product = remoteProductService.getProductById(item.getItemId()).getData();
            if (product != null) {
                itemVO.setItemName(product.getName());
                itemVO.setItemSku(product.getSku());
            }
            itemVO.setQty(item.getQty());
            itemVO.setCostPrice(item.getCostPrice());
            itemVO.setSubtotal(item.getSubtotal());
            return itemVO;
        }).collect(Collectors.toList()));

        return vo;
    }

    @Override
    @Transactional
    @CacheEvict(value = "purchase_orders", allEntries = true)
    public void deleteAllOrders() {
        purchaseOrderItemMapper.deleteAll();
        purchaseOrderMapper.deleteAll();
    }

    @Override
    @Transactional
    @CacheEvict(value = "purchase_orders", allEntries = true)
    public void deleteOrder(Long id) {
        purchaseOrderItemMapper.deleteByOrderId(id);
        purchaseOrderMapper.deleteById(id);
    }
}
