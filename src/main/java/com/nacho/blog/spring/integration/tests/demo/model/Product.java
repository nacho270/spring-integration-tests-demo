package com.nacho.blog.spring.integration.tests.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Data
@Entity
@Table(name = "T_PRODUCT")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Product {

  @Id
  @Column(name = "id", nullable = false, columnDefinition = "varchar(36)")
  @Type(type = "uuid-char")
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "price", nullable = false)
  private BigDecimal price;
}
