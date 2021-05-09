Feature: Shipment lifecycle

  Scenario: Can create a shipment
    Given a product with name 'laptop' and a price of 1000.00 is created
    When a shipment is created for user 1 with 2 items of the last product
    Then the shipment payment is 'PENDING'
    And the shipment total is 2000.00