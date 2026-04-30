package com.example.order.feign;

import com.example.common.domain.Result;
import com.example.common.vo.CustomerVO;
import com.example.common.vo.ProductVO;
import com.example.common.vo.SupplierVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", contextId = "remoteProductService")
public interface RemoteProductService {

    @GetMapping("/products/{id}")
    Result<ProductVO> getProductById(@PathVariable("id") Long id);

    @GetMapping("/suppliers/{id}")
    Result<SupplierVO> getSupplierById(@PathVariable("id") Long id);

    @GetMapping("/customers/{id}")
    Result<CustomerVO> getCustomerById(@PathVariable("id") Long id);
}
