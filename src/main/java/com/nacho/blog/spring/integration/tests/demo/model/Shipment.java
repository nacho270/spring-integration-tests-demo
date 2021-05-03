package com.nacho.blog.spring.integration.tests.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Entity
@Table(name = "T_SHIPMENT")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Shipment {

  @Id
  @Column(name = "id", nullable = false, columnDefinition = "varchar(36)")
  @Type(type = "uuid-char")
  private UUID id;

  @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "f_shipment_id", columnDefinition = "varchar(36)")
  @Type(type = "uuid-char")
  private List<Item> items;

  @Column(name = "total", nullable = false)
  private BigDecimal total;

  @ManyToOne
  @JoinColumn(name = "f_user_id")
  private User user;

  @Enumerated(EnumType.STRING)
  private ShipmentPaymentStatus paymentStatus;

  public enum ShipmentPaymentStatus {
    PENDING, PAID;
  }
}
