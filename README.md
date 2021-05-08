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

- docker run --name spring-int-testing-demo -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -d mysql:latest
  
- docker run --name mongo -p 27017:27017 -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=root -d mongo:latest
