package com.nacho.blog.spring.integration.tests.demo.repository;

import com.nacho.blog.spring.integration.tests.demo.model.LogEntry;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LogRepository extends MongoRepository<LogEntry, String> {
}
