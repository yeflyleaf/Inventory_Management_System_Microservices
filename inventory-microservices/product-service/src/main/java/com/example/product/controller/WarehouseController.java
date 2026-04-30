package com.example.product.controller;

import com.example.common.domain.Result;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;

@RestController
@RequestMapping("/warehouses")
@CrossOrigin
public class WarehouseController {
    @GetMapping
    public Result<Object> getAll() {
        return Result.success(Collections.emptyList());
    }
}
