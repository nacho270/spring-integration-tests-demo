package com.nacho.blog.spring.integration.tests.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.UUID;

@Data
@Entity
@Table(name = "T_ITEM")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Item {

  @Id
  @Column(name = "id", nullable = false)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "f_product_id")
  private Product product;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;
}
