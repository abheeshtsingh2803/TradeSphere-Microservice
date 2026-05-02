package com.tradesphere.inventoryservice.repository;

import com.tradesphere.inventoryservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product, String> {
}
