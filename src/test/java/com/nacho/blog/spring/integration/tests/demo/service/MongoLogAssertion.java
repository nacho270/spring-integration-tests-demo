package com.nacho.blog.spring.integration.tests.demo.service;

import com.nacho.blog.spring.integration.tests.demo.model.LogEntry;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.ListAssert;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MongoLogAssertion extends AbstractAssert<MongoLogAssertion, MongoTemplate> {

  public MongoLogAssertion(MongoTemplate mongoTemplate) {
    super(mongoTemplate, MongoLogAssertion.class);
  }

  public static MongoLogAssertion assertThatMongo(MongoTemplate mongoTemplate) {
    assertThat(mongoTemplate).isNotNull();
    return new MongoLogAssertion(mongoTemplate);
  }

  public MongoQueryBuilder query() {
    return new MongoQueryBuilder(new Query(), actual);
  }

  public record MongoQueryBuilder(Query query, MongoTemplate mongoTemplate) {

    public MongoQueryBuilder forEvent(LogEntry.LogEntryType logEntryType) {
      query.addCriteria(Criteria.where("event").is(logEntryType.toString()));
      return this;
    }

    public MongoQueryBuilder forClass(Class<?> clazz) {
      query.addCriteria(Criteria.where("className").is(clazz.getName()));
      return this;
    }

    public MongoQueryBuilder forIdentifier(UUID identifier) {
      query.addCriteria(Criteria.where("identifier").is(identifier.toString()));
      return this;
    }

    public ListAssert<LogEntry> find() {
      return new ListAssert<>(mongoTemplate.find(query, LogEntry.class));
    }
  }
}
