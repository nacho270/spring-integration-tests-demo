package com.nacho.blog.spring.integration.tests.demo.service;

import com.google.common.collect.ImmutableList;
import org.apache.commons.compress.utils.Lists;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;

public final class KafkaConsumerUtils {

  public static List<ShipmentService.ShipmentCreated> getAtLeastShipmentNewsForPredicate(int msgCount,
                                                                                  Predicate<ShipmentService.ShipmentCreated> predicate,
                                                                                  Consumer<String, ShipmentService.ShipmentCreated> consumer) {
    List<ShipmentService.ShipmentCreated> messages = Lists.newArrayList();
    await()
            .pollDelay(2, SECONDS)
            .atMost(1, MINUTES)
            .pollInterval(500, MILLISECONDS)
            .until(() -> {
              ConsumerRecords<String, ShipmentService.ShipmentCreated> records = KafkaTestUtils.getRecords(consumer, SECONDS.toMillis(1));
              messages.addAll(ImmutableList.copyOf(records.iterator())
                                      .stream()
                                      .map(ConsumerRecord::value)
                                      .map(ShipmentService.ShipmentCreated.class::cast)
                                      .filter(predicate)
                                      .collect(Collectors.toList()));
              return messages.size() >= msgCount;
            });
    return messages;
  }
}
