package com.nacho.blog.spring.integration.tests.demo.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "LogEntry")
public class LogEntry {

  @Id
  private String id;

  private LogEntryType event;
  private String className;
  private String identifier;
  private LocalDateTime dateTime;

  public LogEntry(LogEntryType event, String className, String identifier, LocalDateTime dateTime) {
    this.event = event;
    this.dateTime = dateTime;
    this.className = className;
    this.identifier = identifier;
  }

  public enum LogEntryType {
    CREATE, DELETE
  }
}
