package com.nacho.blog.spring.integration.tests.demo.controller;

import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateProductRequest;
import com.nacho.blog.spring.integration.tests.demo.model.Product;
import com.nacho.blog.spring.integration.tests.demo.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("product")
public class ProductController {

  @Autowired
  private ProductService productService;

  @GetMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<List<Product>> list() {
    return ResponseEntity.ok(productService.getProducts());
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Product> create(@RequestBody final CreateProductRequest createProductRequest) {
    return ResponseEntity.ok(productService.createProduct(createProductRequest.name(), createProductRequest.price()));
  }

  @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Product> getById(@PathVariable("id") UUID id) {
    return productService
            .getById(id)
            .map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
  }
}
