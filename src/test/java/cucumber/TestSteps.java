package cucumber;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateProductRequest;
import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateShipmentRequest;
import com.nacho.blog.spring.integration.tests.demo.model.LogEntry;
import com.nacho.blog.spring.integration.tests.demo.model.Product;
import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import com.nacho.blog.spring.integration.tests.demo.service.ShipmentService;
import com.nacho.blog.spring.integration.tests.demo.service.kafka.PaymentListener;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java8.En;
import io.restassured.RestAssured;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.List;

import static com.nacho.blog.spring.integration.tests.demo.service.KafkaConsumerUtils.getAtLeastShipmentNewsForPredicate;
import static com.nacho.blog.spring.integration.tests.demo.service.MongoLogAssertion.assertThatMongo;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class TestSteps implements En {

  @Autowired
  private TestContextData testContextData;

  @Autowired
  private WireMockServer wireMockServer;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Consumer<String, ShipmentService.ShipmentCreated> shipmentNewsConsumer;

  @Autowired
  private KafkaTemplate<String, PaymentListener.ShipmentPaymentInfo> shipmentPaymentKafkaTemplate;

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

  @Then("the shipment payment status is {string}")
  public void assertShipmentPaymentStatus(String paymentStatus) {
    await()
            .pollDelay(2, SECONDS)
            .atMost(1, MINUTES)
            .pollInterval(500, MILLISECONDS)
            .untilAsserted(() -> {
              testContextData.shipment = RestAssured
                                                 .with()
                                                 .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                                                 .get("/shipment/" + testContextData.shipment.getId())
                                                 .then()
                                                 .statusCode(200)
                                                 .extract().response().as(Shipment.class);

              assertThat(testContextData.shipment.getPaymentStatus())
                      .isEqualTo(Shipment.ShipmentPaymentStatus.valueOf(paymentStatus));
            });
  }

  @And("the shipment total is {double}")
  public void assertShipmentTotal(Double total) {
    assertThat(testContextData.shipment.getTotal())
            .isEqualByComparingTo(BigDecimal.valueOf(total));
  }

  @And("there is a log entry for the shipment")
  public void assertLogEntry() {
    assertThatMongo(mongoTemplate)
            .query()
            .forEvent(LogEntry.LogEntryType.CREATE)
            .forClass(Shipment.class)
            .forIdentifier(testContextData.shipment.getId())
            .find()
            .hasSize(1);
  }

  @And("a shipment created news is sent")
  public void assertShipmentNewsSent() {
    List<ShipmentService.ShipmentCreated> expectedShipmentNews =
            getAtLeastShipmentNewsForPredicate(
                    1,
                    (shipmentCreatedMsg) -> shipmentCreatedMsg.getShipmentId().equals(testContextData.shipment.getId()),
                    shipmentNewsConsumer
            );

    assertThat(expectedShipmentNews)
            .asList()
            .hasSize(1);
  }

  @Then("a payment outcome of {string} is sent")
  public void sendPaymentOutcome(String paymentOutcome) {
    shipmentPaymentKafkaTemplate.send("payment_outcome",
            new PaymentListener.ShipmentPaymentInfo(testContextData.shipment.getId(),
                    Shipment.ShipmentPaymentStatus.valueOf(paymentOutcome)));
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
