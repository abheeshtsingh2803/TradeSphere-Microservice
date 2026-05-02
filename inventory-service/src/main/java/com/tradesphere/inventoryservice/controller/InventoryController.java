package com.tradesphere.inventoryservice.controller;

import com.tradesphere.inventoryservice.dto.InventoryResponse;
import com.tradesphere.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService  inventoryService;

//    @GetMapping("/{sku-code}") -> We will get only one product detail about inventory
    // http://localhost:8082/api/inventory/iphone-13, iphone-13-red

    // http://localhost:8082/api/inventory/?sku-code=iphone-13&sku-code=iphone-13-red -> We will get multiple product details about inventory
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<InventoryResponse> isInStock(@RequestParam List<String> skuCode) {
        return inventoryService.isInStock(skuCode);
    }
}
