package com.nacho.blog.spring.integration.tests.demo.repository;

import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface ShipmentRepository extends CrudRepository<Shipment, UUID> {

  @Modifying
  @Query("UPDATE Shipment s SET s.paymentStatus = :paymentStatus WHERE s.id = :id")
  void markAs(@Param("id") UUID shipmentId, @Param("paymentStatus") Shipment.ShipmentPaymentStatus paymentStatus);

  default void markAsPaid(UUID id) {
    markAs(id, Shipment.ShipmentPaymentStatus.PAID);
  }
}
