Feature: Shipment lifecycle

  Background:
    Given a product with name 'laptop' and a price of 1000.00 is created

  Scenario: Can create a shipment
    When a shipment is created for user 1 with 2 items of the last product
    Then the shipment payment status is PENDING
    And the shipment total is 2000.00
    And there is a log entry for the shipment
    And a shipment created news is sent

  Scenario Outline: Can process payment outcome
    When a shipment is created for user 1 with 2 items of the last product
    Then a shipment created news is sent
    Then a payment outcome of '<PAYMENT_OUTCOME>' is sent
    And the shipment payment status is <PAYMENT_OUTCOME>

    Examples:
      | PAYMENT_OUTCOME |
      | PAID            |
      | REJECTED        |

  Scenario: Should retry payment outcome upon failure
    Given saving the shipment payment status fails once but works the second time
    When a shipment is created for user 1 with 2 items of the last product
    Then a shipment created news is sent
    Then a payment outcome of 'PAID' is sent
    And the shipment payment status is PAID