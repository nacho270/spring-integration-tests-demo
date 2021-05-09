package com.nacho.blog.spring.integration.tests.demo.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateShipmentRequest;
import com.nacho.blog.spring.integration.tests.demo.model.LogEntry;
import com.nacho.blog.spring.integration.tests.demo.model.Product;
import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import com.nacho.blog.spring.integration.tests.demo.model.User;
import com.nacho.blog.spring.integration.tests.demo.repository.UserRepository;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static com.nacho.blog.spring.integration.tests.demo.service.KafkaConsumerUtils.assertShipmentNews;
import static com.nacho.blog.spring.integration.tests.demo.service.MongoLogAssertion.assertThatMongo;
import static com.nacho.blog.spring.integration.tests.demo.service.ShipmentAssertion.assertThatShipment;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@EmbeddedKafka(
        partitions = 1,
        topics = {"shipment_news"},
        brokerProperties = {"listeners=PLAINTEXT://localhost:29092", "port=29092"})
class ShipmentServiceTest {

  @Autowired
  private ShipmentService shipmentService;

  @Autowired
  private ProductService productService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private WireMockServer wireMockServer;

  @Autowired
  private Consumer<String, ShipmentService.ShipmentCreated> shipmentNewsConsumer;

  @BeforeEach
  public void init() {
    wireMockServer.resetAll();
  }

  @Test
  @DisplayName("Can persist a user, a product and create a shipment with them")
  void testCreateUserProductAndShipment() {

    // given
    var product = productService.createProduct("my product", BigDecimal.TEN);
    var user = userRepository.save(new User(1, "test@user.com"));
    var request = new CreateShipmentRequest(1, List.of(new CreateShipmentRequest.ItemRequest(product.getId(), 1)));

    // when
    var shipment = shipmentService.createShipment(request);

    // then
    assertShipmentCreation(product, user, shipment);

  }

  @Test
  @DisplayName("Can persist a product and create a shipment with it and a user from the api")
  void testCreateProductAndShipment() {

    // given
    var product = productService.createProduct("my product", BigDecimal.TEN);
    wireMockServer.stubFor(
            WireMock.get("/users/1")
                    .willReturn(WireMock.aResponse()
                                        .withStatus(200)
                                        .withBody("""
                                                {
                                                  "id": 1,
                                                  "email": "test@user.com"
                                                }
                                                """)));
    var request = new CreateShipmentRequest(1, List.of(new CreateShipmentRequest.ItemRequest(product.getId(), 1)));

    // when
    var shipment = shipmentService.createShipment(request);

    // then
    assertShipmentCreation(product, new User(1, "test@user.com"), shipment);

  }

  private void assertShipmentCreation(Product product, User user, Shipment shipment) {
    assertThatShipment(shipment)
            .hasPendingPayment()
            .totalIs(BigDecimal.TEN)
            .userIs(user)
            .itemCountIs(1)
            .itemAt(0)
            .hasProduct(product);

    assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "T_SHIPMENT",
            "id = '" + shipment.getId() + "'"))
            .isEqualTo(1);

    assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "T_PRODUCT",
            "id = '" + product.getId() + "'"))
            .isEqualTo(1);

    assertThat(JdbcTestUtils.countRowsInTableWhere(jdbcTemplate, "T_ITEM",
            "f_product_id = '" + product.getId() + "' and f_shipment_id = '" + shipment.getId() + "'"))
            .isEqualTo(1);

    assertThatMongo(mongoTemplate)
            .query()
            .forEvent(LogEntry.LogEntryType.CREATE)
            .forClass(Product.class)
            .forIdentifier(product.getId())
            .find()
            .hasSize(1);

    assertThatMongo(mongoTemplate)
            .query()
            .forEvent(LogEntry.LogEntryType.CREATE)
            .forClass(Shipment.class)
            .forIdentifier(shipment.getId())
            .find()
            .hasSize(1);

    assertShipmentNews(shipmentNewsConsumer, shipment);

  }

  @TestConfiguration
  static class KafkaTestConfiguration {

    @Bean
    Consumer<String, ShipmentService.ShipmentCreated> shipmentNewsConsumer(EmbeddedKafkaBroker embeddedKafka) {
      final Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("intTestDemo", "true", embeddedKafka);
      consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
      consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
      Consumer<String, ShipmentService.ShipmentCreated> consumer =
              new DefaultKafkaConsumerFactory<>(
                      consumerProps,
                      new StringDeserializer(),
                      new JsonDeserializer<>(ShipmentService.ShipmentCreated.class, false)
              ).createConsumer();
      embeddedKafka.consumeFromEmbeddedTopics(consumer, "shipment_news");
      return consumer;
    }
  }

}