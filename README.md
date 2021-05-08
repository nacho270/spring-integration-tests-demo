# Spring integration tests demo

- Receive an API call
- Make an API call
- Store MySql
- Store in MongoDB
- Receive from kafka
- Send to kafka

## Notes

- Not doing unit tests as it's not the idea of this project.

- Demonstrate Spring Integration tests and Cucumber for BDD/Acceptance test

## Run locally

- `docker compose up`

To send payment notification:

`docker exec -it spring-integration-tests-demo_kafka_1 bash`

`/usr/bin/kafka-console-producer.sh --topic payment_outcome --broker-list localhost:9092`

`{"shipmentId":"fe983d38-4384-40f0-baf9-684ef71a2491", "paymentStatus":"PAID"}`

`kafka-console-consumer.sh --topic shipment_news --bootstrap-server localhost:9092`