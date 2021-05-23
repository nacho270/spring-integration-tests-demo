package cucumber.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nacho.blog.spring.integration.tests.demo.Application;
import com.nacho.blog.spring.integration.tests.demo.service.ShipmentService;
import com.nacho.blog.spring.integration.tests.demo.service.WireMockInitializer;
import com.nacho.blog.spring.integration.tests.demo.service.kafka.PaymentListener;
import cucumber.TestContextData;
import io.cucumber.spring.CucumberContextConfiguration;
import io.restassured.RestAssured;
import io.restassured.config.ObjectMapperConfig;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

@ActiveProfiles("test")
@CucumberContextConfiguration
@ExtendWith(SpringExtension.class)
@EmbeddedKafka(topics = {"shipment_news", "payment_outcome"})
@ContextConfiguration(initializers = {WireMockInitializer.class})
@SpringBootTest(
        webEnvironment = WebEnvironment.RANDOM_PORT,
        classes = {
                Application.class, TestContextData.class,
                CucumberConfig.KafkaTestConfiguration.class,
                CucumberConfig.RestAssuredConfig.class
        }
)
public class CucumberConfig {

  @TestConfiguration
  static class KafkaTestConfiguration {

    @Bean
    Consumer<String, ShipmentService.ShipmentCreated> shipmentNewsConsumer(EmbeddedKafkaBroker embeddedKafka) {
      final Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("newsConsumer", "true", embeddedKafka);
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

    @Bean
    public KafkaTemplate<String, PaymentListener.ShipmentPaymentInfo> shipmentPaymentKafkaTemplate(KafkaProperties kafkaProperties) {
      return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
              kafkaProperties.buildProducerProperties(),
              new StringSerializer(),
              new JsonSerializer<>()));
    }
  }

  @TestConfiguration
  @RequiredArgsConstructor
  static class RestAssuredConfig extends ServerPortInfoApplicationContextInitializer implements InitializingBean {

    private final ObjectMapper objectMapper;
    private final Environment env;

    @Override
    public void afterPropertiesSet() {
      var objectMapperConfig = new ObjectMapperConfig()
                                       .jackson2ObjectMapperFactory((type, s) -> objectMapper);
      RestAssured.config = io.restassured.config.RestAssuredConfig
                                   .config()
                                   .objectMapperConfig(objectMapperConfig);
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
      super.onApplicationEvent(event);
      Integer serverPort = env.getProperty("local.server.port", Integer.class);
      if (serverPort != null) {
        RestAssured.baseURI = "http://localhost:" + serverPort;
      }
    }
  }
}
