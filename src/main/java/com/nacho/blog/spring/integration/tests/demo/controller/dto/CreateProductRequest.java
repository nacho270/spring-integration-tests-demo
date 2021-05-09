package com.nacho.blog.spring.integration.tests.demo.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record CreateProductRequest(@JsonProperty("name") String name, @JsonProperty("price") BigDecimal price) {

}
