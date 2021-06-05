package com.nacho.blog.spring.integration.tests.demo.controller;

import com.google.gson.Gson;
import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateProductRequest;
import com.nacho.blog.spring.integration.tests.demo.model.Product;
import com.nacho.blog.spring.integration.tests.demo.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@WebMvcTest(ProductController.class)
class ProductControllerTest {

  @MockBean
  private ProductService productService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  @DisplayName("Get existing product by id should return HTTP OK")
  void testGetExistingProductById() throws Exception {
    // given
    var product = Product.builder()
                          .id(UUID.randomUUID())
                          .name("a product")
                          .build();
    when(productService.getById(any())).thenReturn(Optional.of(product));

    // when
    mockMvc.perform(get("/product/" + product.getId()))
            .andDo(print())
            // then
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(product.getId().toString()))
            .andExpect(jsonPath("$.name").value(product.getName()));
  }

  @Test
  @DisplayName("Get non existing product by id should return HTTP NOT FOUND")
  void testGetNonExistingProductById() throws Exception {
    // given
    when(productService.getById(any())).thenReturn(Optional.empty());

    // when
    mockMvc.perform(get("/product/" + UUID.randomUUID()))
            .andDo(print())
            // then
            .andExpect(status().isNotFound());
  }

  @Test
  @DisplayName("Create a product should return HTTP CREATED")
  void testCreateProduct() throws Exception {
    // given
    var createProductRequest = new CreateProductRequest("a product", BigDecimal.TEN);
    var product = Product.builder()
                          .id(UUID.randomUUID())
                          .name(createProductRequest.name())
                          .price(createProductRequest.price())
                          .build();
    when(productService.createProduct(createProductRequest.name(), createProductRequest.price())).thenReturn(product);

    // when
    mockMvc.perform(post("/product")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(new Gson().toJson(createProductRequest)))
            // then
            .andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(product.getId().toString()))
            .andExpect(jsonPath("$.name").value(product.getName()));
  }
}