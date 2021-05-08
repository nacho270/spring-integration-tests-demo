package com.nacho.blog.spring.integration.tests.demo.service;

import com.nacho.blog.spring.integration.tests.demo.model.Item;
import com.nacho.blog.spring.integration.tests.demo.model.Product;
import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import com.nacho.blog.spring.integration.tests.demo.model.User;
import org.assertj.core.api.AbstractAssert;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * An example of an Assertj custom assertion.
 */
public class ShipmentAssertion extends AbstractAssert<ShipmentAssertion, Shipment> {

  public ShipmentAssertion(Shipment shipment) {
    super(shipment, ShipmentAssertion.class);
  }

  public static ShipmentAssertion assertThatShipment(Shipment shipment) {
    assertThat(shipment).isNotNull();
    return new ShipmentAssertion(shipment);
  }

  public ShipmentAssertion hasPendingPayment() {
    assertThat(actual.getPaymentStatus()).isEqualTo(Shipment.ShipmentPaymentStatus.PENDING);
    return this;
  }

  public ShipmentAssertion isPaid() {
    assertThat(actual.getPaymentStatus()).isEqualTo(Shipment.ShipmentPaymentStatus.PAID);
    return this;
  }

  public ShipmentAssertion totalIs(BigDecimal total) {
    assertThat(actual.getTotal()).isEqualByComparingTo(total);
    return this;
  }

  public ShipmentAssertion userIs(User user) {
    assertThat(actual.getUser()).isNotNull();
    assertThat(actual.getUser().getEmail()).isEqualTo(user.getEmail());
    assertThat(actual.getUser().getId()).isEqualTo(user.getId());
    return this;
  }

  public ShipmentAssertion itemCountIs(int itemCount) {
    assertThat(actual.getItems()).isNotNull().hasSize(itemCount);
    return this;
  }

  /**
   * Creates an assertion instance of an item.
   * When calling this method, the DSL is no longer asserting on the shipment.
   */
  public ItemCustomAssertion itemAt(int itemIndex) {
    return new ItemCustomAssertion(actual.getItems().get(0));
  }

  /**
   * An example of a nested custom assertion.
   */
  public static class ItemCustomAssertion extends AbstractAssert<ItemCustomAssertion, Item> {

    public ItemCustomAssertion(Item item) {
      super(item, ItemCustomAssertion.class);
    }

    public void hasProduct(Product product){
      assertThat(actual.getProduct()).isNotNull();
      assertThat(actual.getProduct().getId()).isEqualTo(product.getId());
    }
  }
}
