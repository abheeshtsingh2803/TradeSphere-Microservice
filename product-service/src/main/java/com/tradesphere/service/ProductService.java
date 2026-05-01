package com.tradesphere.service;

import com.tradesphere.dto.ProductRequest;
import com.tradesphere.dto.ProductResponse;
import com.tradesphere.model.Product;
import com.tradesphere.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public void createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.getName())
                .description(productRequest.getDescription())
                .price(productRequest.getPrice())
                .build();

        productRepository.save(product);

        log.info("Product {} has been created", product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        List<Product> products = productRepository.findAll();

        return products.stream().map(this::mapToProductResponse).toList();
    }

    private ProductResponse mapToProductResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .build();
    }

    public ProductResponse getProductById(String id) {
        return  productRepository.findById(id).map(this::mapToProductResponse).orElse(null);
    }

    public void updateProduct(String id, ProductRequest productRequest) {
        if(productRepository.findById(id).isPresent()) {
            Product product = productRepository.findById(id).get();
            if(productRequest.getName() != null)
                product.setName(productRequest.getName());

            if(productRequest.getDescription() != null)
                product.setDescription(productRequest.getDescription());

            if(productRequest.getPrice() != null)
                product.setPrice(productRequest.getPrice());

            productRepository.save(product);
        }
    }

    public void deleteProduct(String id) {
        if(productRepository.findById(id).isPresent()) {
            productRepository.deleteById(id);
        }
    }
}
