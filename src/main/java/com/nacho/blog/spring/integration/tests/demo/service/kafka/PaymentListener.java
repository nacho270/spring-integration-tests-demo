package com.nacho.blog.spring.integration.tests.demo.service.kafka;

import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import com.nacho.blog.spring.integration.tests.demo.service.ShipmentService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentListener {

  @Autowired
  private ShipmentService shipmentService;

  @KafkaListener(topics = "${payments.topic}", containerFactory = "kafkaJsonListenerContainerFactory")
  public void processMessage(ShipmentPaymentInfo paymentInfo) {
    shipmentService.updatePaymentStatus(paymentInfo.getShipmentId(), paymentInfo.getPaymentStatus());
  }

  @Data
  @AllArgsConstructor
  @NoArgsConstructor
  public static class ShipmentPaymentInfo {
    private UUID shipmentId;
    private Shipment.ShipmentPaymentStatus paymentStatus;
  }
}
