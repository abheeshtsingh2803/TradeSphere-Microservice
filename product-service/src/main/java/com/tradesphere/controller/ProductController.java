package com.tradesphere.controller;

import com.tradesphere.dto.ProductRequest;
import com.tradesphere.dto.ProductResponse;
import com.tradesphere.inventoryservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/products")
@RequiredArgsConstructor
public class ProductController {

    final ProductService productService;

    @PostMapping("/create")
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@RequestBody ProductRequest productRequest) {
        productService.createProduct(productRequest);
    }

    @GetMapping("/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ProductResponse getProductById(@PathVariable String id) {
        return productService.getProductById(id);
    }

    @PatchMapping("/{id}/update")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProduct(@PathVariable String id,  @RequestBody ProductRequest productRequest) {
        productService.updateProduct(id, productRequest);
    }

    @DeleteMapping("/{id}/delete")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }
}
