package com.tradesphere.repository;

import com.tradesphere.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ProductRepository extends MongoRepository<Product, String> {
}
