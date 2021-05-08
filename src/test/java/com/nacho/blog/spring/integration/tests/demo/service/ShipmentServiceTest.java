package com.nacho.blog.spring.integration.tests.demo.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.nacho.blog.spring.integration.tests.demo.controller.dto.CreateShipmentRequest;
import com.nacho.blog.spring.integration.tests.demo.model.User;
import com.nacho.blog.spring.integration.tests.demo.repository.LogRepository;
import com.nacho.blog.spring.integration.tests.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static com.nacho.blog.spring.integration.tests.demo.service.ShipmentCustomAssertion.assertThatShipment;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {WireMockInitializer.class})
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
//@DataMongoTest
class ShipmentServiceTest {

  @Autowired
  private ShipmentService shipmentService;

  @Autowired
  private ProductService productService;

  @Autowired
  private JdbcTemplate jdbcTemplate;

//  @Autowired
//  private MongoTemplate mongoTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private WireMockServer wireMockServer;

  @MockBean
  private LogRepository logRepository;

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
    when(logRepository.save(any())).thenReturn(null);

    // when
    var shipment = shipmentService.createShipment(request);

    // then
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

//    assertThat(mongoTemplate.find(
//            new Query()
//                    .addCriteria(Criteria.where("event").is(LogEntry.LogEntryType.CREATE.toString()))
//                    .addCriteria(Criteria.where("className").is(Product.class.getName()))
//                    .addCriteria(Criteria.where("identifier").is(product.getId().toString())), LogEntry.class, "log")
//    ).asList().hasSize(1);
//
//    assertThat(mongoTemplate.find(
//            new Query()
//                    .addCriteria(Criteria.where("event").is(LogEntry.LogEntryType.CREATE.toString()))
//                    .addCriteria(Criteria.where("className").is(Shipment.class.getName()))
//                    .addCriteria(Criteria.where("identifier").is(shipment.getId().toString())), LogEntry.class, "log")
//    ).asList().hasSize(1);

  }

  @Test
  @DisplayName("Can a product and create a shipment with it and a user from the api")
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
    when(logRepository.save(any())).thenReturn(null);

    // when
    var shipment = shipmentService.createShipment(request);

    // then
    assertThatShipment(shipment)
            .hasPendingPayment()
            .totalIs(BigDecimal.TEN)
            .userIs(new User(1, "test@user.com"))
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

//    assertThat(mongoTemplate.find(
//            new Query()
//                    .addCriteria(Criteria.where("event").is(LogEntry.LogEntryType.CREATE.toString()))
//                    .addCriteria(Criteria.where("className").is(Product.class.getName()))
//                    .addCriteria(Criteria.where("identifier").is(product.getId().toString())), LogEntry.class, "log")
//    ).asList().hasSize(1);
//
//    assertThat(mongoTemplate.find(
//            new Query()
//                    .addCriteria(Criteria.where("event").is(LogEntry.LogEntryType.CREATE.toString()))
//                    .addCriteria(Criteria.where("className").is(Shipment.class.getName()))
//                    .addCriteria(Criteria.where("identifier").is(shipment.getId().toString())), LogEntry.class, "log")
//    ).asList().hasSize(1);

  }

}