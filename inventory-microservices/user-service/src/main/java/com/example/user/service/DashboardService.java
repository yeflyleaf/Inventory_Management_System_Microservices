package com.example.user.service;

import com.example.user.vo.DashboardVO;
import com.example.user.vo.CategoryStatsVO;
import java.util.List;

public interface DashboardService {
    DashboardVO getStats(Long itemId, Integer days, Long warehouseId, String category, String itemName);
    List<CategoryStatsVO> getCategoryStats(String type);
}
