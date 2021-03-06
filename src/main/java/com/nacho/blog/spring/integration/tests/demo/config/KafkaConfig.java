package com.nacho.blog.spring.integration.tests.demo.config;

import com.nacho.blog.spring.integration.tests.demo.service.ShipmentService;
import com.nacho.blog.spring.integration.tests.demo.service.kafka.PaymentListener.ShipmentPaymentInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
@EnableKafka
public class KafkaConfig {

  @Bean
  public KafkaTemplate<String, ShipmentService.ShipmentCreated> shipmentCreatedKafkaTemplate(KafkaProperties kafkaProperties) {
    return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(
            kafkaProperties.buildProducerProperties(),
            new StringSerializer(),
            new JsonSerializer<>()));
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, ShipmentPaymentInfo> kafkaJsonListenerContainerFactory(KafkaProperties kafkaProperties) {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, ShipmentPaymentInfo>();
    factory.setConsumerFactory(
            new DefaultKafkaConsumerFactory<>(
                    kafkaProperties.buildConsumerProperties(),
                    new StringDeserializer(),
                    new ErrorHandlingDeserializer<>(new JsonDeserializer<>(ShipmentPaymentInfo.class))));
    return factory;
  }
}
