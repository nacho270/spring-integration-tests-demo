package com.nacho.blog.spring.integration.tests.demo.controller.dto;

import java.util.List;
import java.util.UUID;

public record CreateShipmentRequest(Integer userId, List<ItemRequest> items) {

  public static record ItemRequest(UUID product, Integer quantity) {
  }
}
