package com.nacho.blog.spring.integration.tests.demo.service;

import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateShipmentRequest;
import com.nacho.blog.spring.integration.tests.demo.model.Item;
import com.nacho.blog.spring.integration.tests.demo.model.LogEntry;
import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import com.nacho.blog.spring.integration.tests.demo.repository.LogRepository;
import com.nacho.blog.spring.integration.tests.demo.repository.ProductRepository;
import com.nacho.blog.spring.integration.tests.demo.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ShipmentService {

  @Autowired
  private final ShipmentRepository shipmentRepository;

  @Autowired
  private final ProductRepository productRepository;

  @Autowired
  private final UserService userService;

  @Autowired
  private final LogRepository logRepository;

  public Optional<Shipment> getById(final UUID id) {
    return shipmentRepository.findById(id);
  }

  public Shipment createShipment(final CreateShipmentRequest createShipmentRequest) {
    try {
      Shipment shipment = shipmentRepository.save(mapShipment(createShipmentRequest));
      logRepository.save(new LogEntry(LogEntry.LogEntryType.CREATE, Shipment.class.getName(),
              shipment.getId().toString(), LocalDateTime.now()));
      return shipment;
    } catch (Exception e) {
      log.error("Error saving shipment: {}", e.getMessage());
      throw e;
    }
  }

  public Integer getShipmentCount() {
    return (int) shipmentRepository.count();
  }

  public void clearShipments() {
    shipmentRepository.deleteAll();
    logRepository.save(new LogEntry(LogEntry.LogEntryType.DELETE, Shipment.class.getName(), "all",
            LocalDateTime.now()));
  }

  private Shipment mapShipment(final CreateShipmentRequest createShipmentRequest) {
    var items = createShipmentRequest.items().stream()
            .map(this::mapItem)
            .collect(toList());
    return Shipment.builder()
            .id(UUID.randomUUID())
            .items(items)
            .paymentStatus(Shipment.ShipmentPaymentStatus.PENDING)
            .user(userService.getById(createShipmentRequest.userId()))
            .total(items.stream()
                    .map(it -> it.getProduct().getPrice().multiply(BigDecimal.valueOf(it.getQuantity().longValue())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add))
            .build();
  }

  private Item mapItem(final CreateShipmentRequest.ItemRequest ir) {
    return Item.builder() //
            .id(UUID.randomUUID())
            .product(productRepository.findById(ir.product()).orElseThrow(() -> new RuntimeException("Product not found")))
            .quantity(ir.quantity())
            .build();
  }

  public void markAsPaid(UUID shipmentId) {
    // TODO: trigger this from kafka
    shipmentRepository.markAsPaid(shipmentId);
  }
}
