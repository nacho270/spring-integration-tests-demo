package com.nacho.blog.spring.integration.tests.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "T_USER")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {

  @Id
  @Column(name = "id", nullable = false)
  private Integer id;

  @Column(name = "email", nullable = false)
  private String email;
}
