package com.nacho.blog.spring.integration.tests.demo.service;

import com.google.common.collect.ImmutableList;
import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

final class KafkaConsumerUtils {

  static void assertShipmentNews(Consumer<String, ShipmentService.ShipmentCreated> consumer, Shipment shipment) {
    await()
            .pollDelay(2, SECONDS)
            .atMost(1, MINUTES)
            .pollInterval(500, MILLISECONDS)
            .until(() -> {
              ConsumerRecords<String, ShipmentService.ShipmentCreated> records =
                      KafkaTestUtils.getRecords(consumer, SECONDS.toMillis(1));
              return ImmutableList.copyOf(records.iterator())
                             .stream()
                             .map(ConsumerRecord::value)
                             .map(ShipmentService.ShipmentCreated.class::cast)
                             .filter(sc -> sc.getShipmentId().equals(shipment.getId()))
                             .count() == 1;
            });
  }
}
