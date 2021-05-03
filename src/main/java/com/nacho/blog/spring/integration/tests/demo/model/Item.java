package com.nacho.blog.spring.integration.tests.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

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
  @Column(name = "id", nullable = false, columnDefinition = "varchar(36)")
  @Type(type = "uuid-char")
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "f_product_id", columnDefinition = "varchar(36)")
  @Type(type = "uuid-char")
  private Product product;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;
}
