package com.example.product.service.impl;

import com.example.product.entity.Product;
import com.example.product.entity.StockFlow;
import com.example.product.mapper.ProductMapper;
import com.example.product.mapper.StockFlowMapper;
import com.example.product.service.StockService;
import com.example.product.vo.StockFlowVO;
import com.example.product.vo.StockVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 库存服务实现类
 */
@Service
public class StockServiceImpl implements StockService {

    @Autowired
    private StockFlowMapper stockFlowMapper;

    @Autowired
    private ProductMapper productMapper;

    /**
     * 获取库存快照
     * 计算指定仓库或所有仓库的当前库存状态
     * 包含：当前库存数量、最后变动时间、仓库名称等信息
     * 结果会被缓存，key为 'warehouse:' + warehouseId
     *
     * @param warehouseId 仓库ID (可选，为null则统计所有仓库)
     * @return 库存快照VO列表
     */
    @Override
    @Cacheable(value = "stock_snapshot", key = "'warehouse:' + (#warehouseId == null ? 'all' : #warehouseId)")
    public List<StockVO> getStockSnapshot(Long warehouseId) {
        List<Product> allProducts = productMapper.selectAll();
        Map<Long, Map<String, Object>> lastMovements = stockFlowMapper.selectLastMovements();
        List<StockVO> stockVOs = new ArrayList<>();

        for (Product product : allProducts) {
            Integer currentStock = stockFlowMapper.sumStockByItemIdAndWarehouseId(product.getId(), warehouseId);
            if (currentStock == null) currentStock = 0;

            StockVO vo = new StockVO();
            vo.setItemId(product.getId());
            vo.setItemName(product.getName());
            vo.setItemSku(product.getSku());
            vo.setCategory(product.getCategory());
            vo.setWarehouseId(warehouseId);
            vo.setCurrentStock(currentStock);
            
            if (lastMovements != null && lastMovements.containsKey(product.getId())) {
                Object lastDate = lastMovements.get(product.getId()).get("lastDate");
                if (lastDate instanceof LocalDateTime) {
                    vo.setLastMovementDate((LocalDateTime) lastDate);
                } else if (lastDate instanceof java.sql.Timestamp) {
                    vo.setLastMovementDate(((java.sql.Timestamp) lastDate).toLocalDateTime());
                } else if (lastDate instanceof java.util.Date) {
                    vo.setLastMovementDate(new java.sql.Timestamp(((java.util.Date)lastDate).getTime()).toLocalDateTime());
                }
            }
            stockVOs.add(vo);
        }
        return stockVOs;
    }

    /**
     * 获取指定商品的库存流水记录
     *
     * @param itemId 商品ID
     * @return 库存流水VO列表
     */
    @Override
    public List<StockFlowVO> getStockFlows(Long itemId) {
        return stockFlowMapper.selectByItemId(itemId).stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 获取所有库存流水记录
     *
     * @return 所有库存流水VO列表
     */
    @Override
    public List<StockFlowVO> getAllStockFlows() {
        return stockFlowMapper.selectAll().stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 调整库存
     * 记录一条库存流水，并更新库存状态
     * 严格校验：不允许库存扣减后为负数
     * 操作完成后清空 'stock_snapshot' 缓存
     *
     * @param itemId       商品ID
     * @param changeAmount 变动数量 (正数增加，负数减少)
     * @param changeType   变动类型
     * @param warehouseId  仓库ID
     * @param refType      关联单据类型
     * @param refId        关联单据ID/编号
     * @throws RuntimeException 如果库存不足
     */
    @Override
    @Transactional
    @CacheEvict(value = "stock_snapshot", allEntries = true)
    public void adjustStock(Long itemId, Integer changeAmount, String changeType, Long warehouseId, String refType, String refId) {
        if (changeAmount < 0) {
            Integer currentStock = stockFlowMapper.sumStockByItemIdAndWarehouseId(itemId, warehouseId);
            if (currentStock == null) currentStock = 0;
            if (currentStock + changeAmount < 0) {
                throw new RuntimeException("库存不足，无法执行出库操作。当前库存: " + currentStock + ", 变动: " + changeAmount);
            }
        }

        StockFlow flow = new StockFlow();
        flow.setItemId(itemId);
        flow.setChangeAmount(changeAmount);
        flow.setChangeType(changeType);
        flow.setRefType(refType);
        flow.setRefId(refId);
        flow.setWarehouseId(warehouseId);
        flow.setCreatedAt(LocalDateTime.now());
        stockFlowMapper.insert(flow);
    }

    /**
     * 删除指定商品的所有库存记录
     * 操作完成后清空 'stock_snapshot' 缓存
     *
     * @param itemId 商品ID
     */
    @Override
    @Transactional
    @CacheEvict(value = "stock_snapshot", allEntries = true)
    public void deleteStock(Long itemId) {
        stockFlowMapper.deleteByItemId(itemId);
    }

    private StockFlowVO convertToVO(StockFlow flow) {
        StockFlowVO vo = new StockFlowVO();
        vo.setId(flow.getId());
        vo.setItemId(flow.getItemId());
        vo.setChangeAmount(flow.getChangeAmount());
        vo.setChangeType(flow.getChangeType());
        vo.setRefType(flow.getRefType());
        vo.setRefId(flow.getRefId());
        vo.setWarehouseId(flow.getWarehouseId());
        vo.setCreatedAt(flow.getCreatedAt());
        Product product = productMapper.selectById(flow.getItemId());
        if (product != null) vo.setItemName(product.getName());
        return vo;
    }
}

