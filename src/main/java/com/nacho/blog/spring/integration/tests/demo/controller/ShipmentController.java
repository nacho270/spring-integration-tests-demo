package com.nacho.blog.spring.integration.tests.demo.controller;

import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateShipmentRequest;
import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import com.nacho.blog.spring.integration.tests.demo.service.ShipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("shipment")
public class ShipmentController {

  @Autowired
  private ShipmentService shipmentService;

  @GetMapping(path = "/{id}", produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Shipment> getById(@PathVariable("id") final UUID id) {
    return shipmentService.getById(id)
                   .map(ResponseEntity::ok)
                   .orElseGet(() -> ResponseEntity.notFound().build());
  }

  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<Shipment> create(@RequestBody final CreateShipmentRequest createShipmentRequest) {
    URI currentURI = ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
    return ResponseEntity.created(currentURI).body(shipmentService.createShipment(createShipmentRequest));
  }

  @GetMapping(path = "/count")
  public ResponseEntity<Integer> count() {
    return ResponseEntity.ok(shipmentService.getShipmentCount());
  }

  @DeleteMapping
  public ResponseEntity<Void> clearShipments() {
    shipmentService.clearShipments();
    return ResponseEntity.ok().build();
  }
}
