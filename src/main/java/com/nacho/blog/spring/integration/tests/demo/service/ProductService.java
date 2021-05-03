package com.nacho.blog.spring.integration.tests.demo.service;

import com.nacho.blog.spring.integration.tests.demo.model.LogEntry;
import com.nacho.blog.spring.integration.tests.demo.model.Product;
import com.nacho.blog.spring.integration.tests.demo.repository.LogRepository;
import com.nacho.blog.spring.integration.tests.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toList;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductService {

  @Autowired
  private final ProductRepository productRepository;

  @Autowired
  private final LogRepository logRepository;

  public Product createProduct(String name, BigDecimal price) {
    Product newProduct = productRepository.save(Product.builder()
            .id(UUID.randomUUID())
            .name(name)
            .price(price)
            .build());
    logRepository.save(new LogEntry(LogEntry.LogEntryType.CREATE, Product.class.getName(), newProduct.getId().toString(),
            LocalDateTime.now()));
    return newProduct;
  }

  public List<Product> getProducts() {
    return StreamSupport
            .stream(productRepository.findAll().spliterator(), false)
            .collect(toList());
  }

  public Optional<Product> getById(UUID id) {
    return productRepository.findById(id);
  }
}
