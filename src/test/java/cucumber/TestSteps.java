package cucumber;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateProductRequest;
import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateShipmentRequest;
import com.nacho.blog.spring.integration.tests.demo.model.Product;
import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java8.En;
import io.restassured.RestAssured;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSteps implements En {

  @LocalServerPort
  private int port;

  @Autowired
  private TestContextData testContextData;

  @Autowired
  private WireMockServer wireMockServer;

  @Given("a product with name {string} and a price of {double} is created")
  public void createProduct(String name, Double price) {
    testContextData.product = RestAssured
                                      .with()
                                      .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                      .body(new CreateProductRequest(name, BigDecimal.valueOf(price)))
                                      .post("/product")
                                      .then()
                                      .statusCode(201)
                                      .extract().response().as(Product.class);
  }

  @Then("the shipment payment is {string}")
  public void assertShipmentPaymentStatus(String paymentStatus) {
    assertThat(testContextData.shipment.getPaymentStatus())
            .isEqualTo(Shipment.ShipmentPaymentStatus.valueOf(paymentStatus));
  }

  @And("the shipment total is {double}")
  public void assertShipmentTotal(Double total) {
    assertThat(testContextData.shipment.getTotal())
            .isEqualByComparingTo(BigDecimal.valueOf(total));
  }

  public TestSteps() {
    Before(() -> wireMockServer.resetAll());

    // another option for defining test steps
    When("a shipment is created for user {int} with {int} items of the last product",
            (Integer userId, Integer itemQuantity) -> {
              stubApiCallForUser(userId);
              Product product = testContextData.product;
              var createShipmentRequest = new CreateShipmentRequest(userId,
                      List.of(new CreateShipmentRequest.ItemRequest(product.getId(), itemQuantity)));

              testContextData.shipment = RestAssured
                                                 .with()
                                                 .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                                 .body(createShipmentRequest)
                                                 .post("/shipment")
                                                 .then()
                                                 .statusCode(201)
                                                 .extract().response().as(Shipment.class);
            });
  }

  private void stubApiCallForUser(Integer userId) {
    wireMockServer.stubFor(
            WireMock.get("/users/" + userId)
                    .willReturn(WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody(String.format("""
                                                {
                                                  "id": %s,
                                                  "email": "test@user.com"
                                                }
                                                """, userId))));
  }


}
