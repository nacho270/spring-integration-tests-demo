package com.nacho.blog.spring.integration.tests.demo.repository;

import com.nacho.blog.spring.integration.tests.demo.model.Product;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface ProductRepository extends CrudRepository<Product, UUID> {
}
