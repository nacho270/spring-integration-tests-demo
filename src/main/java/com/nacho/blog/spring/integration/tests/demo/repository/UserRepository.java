package com.nacho.blog.spring.integration.tests.demo.repository;

import com.nacho.blog.spring.integration.tests.demo.model.User;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserRepository extends CrudRepository<User, Integer> {
}
