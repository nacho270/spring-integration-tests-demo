package com.nacho.blog.spring.integration.tests.demo.controller.dto;

import java.math.BigDecimal;

public record CreateProductRequest(String name, BigDecimal price) {

}
