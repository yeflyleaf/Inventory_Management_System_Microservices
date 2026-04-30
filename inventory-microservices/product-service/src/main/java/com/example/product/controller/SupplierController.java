package com.example.product.controller;

import com.example.common.annotation.Log;
import com.example.common.domain.Result;
import com.example.product.dto.SupplierDTO;
import com.example.product.service.SupplierService;
import com.example.common.vo.SupplierVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 供应商管理控制器
 * 提供供应商的增删改查功能
 */
@RestController
@RequestMapping("/suppliers")
@CrossOrigin
public class SupplierController {

    @Autowired
    private SupplierService supplierService;

    /**
     * 获取所有供应商列表
     * @return 供应商列表
     */
    @GetMapping
    public Result<List<SupplierVO>> getAll() {
        List<SupplierVO> suppliers = supplierService.getAllSuppliers();
        return Result.success(suppliers, "获取供应商列表成功");
    }

    @GetMapping("/{id}")
    public Result<SupplierVO> getById(@PathVariable Long id) {
        return Result.success(supplierService.getSupplierById(id), "获取供应商成功");
    }

    /**
     * 新增供应商
     * @param supplierDTO 供应商信息
     * @return 成功信息
     */
    @PostMapping
    @Log(module = "供应商管理", action = "新增供应商", description = "新增供应商信息")
    public Result<Void> add(@RequestBody SupplierDTO supplierDTO) {
        supplierService.addSupplier(supplierDTO);
        return Result.success(null, "添加供应商成功");
    }

    /**
     * 更新供应商信息
     * @param supplierDTO 供应商信息
     * @return 成功信息
     */
    @PutMapping
    @Log(module = "供应商管理", action = "更新供应商", description = "更新供应商信息")
    public Result<Void> update(@RequestBody SupplierDTO supplierDTO) {
        supplierService.updateSupplier(supplierDTO);
        return Result.success(null, "更新供应商成功");
    }

    /**
     * 删除供应商
     * @param id 供应商ID
     * @return 成功信息
     */
    @DeleteMapping("/{id}")
    @Log(module = "供应商管理", action = "删除供应商", description = "删除供应商信息")
    public Result<Void> delete(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return Result.success(null, "删除供应商成功");
    }
}
