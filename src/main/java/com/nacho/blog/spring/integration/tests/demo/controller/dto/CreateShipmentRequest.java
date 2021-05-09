package com.nacho.blog.spring.integration.tests.demo.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public record CreateShipmentRequest(@JsonProperty("userId") Integer userId,
                                    @JsonProperty("items") List<ItemRequest> items) {

  public static record ItemRequest(@JsonProperty("product") UUID product,
                                   @JsonProperty("quantity") Integer quantity) {
  }
}
