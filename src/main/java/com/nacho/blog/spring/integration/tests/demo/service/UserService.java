package com.nacho.blog.spring.integration.tests.demo.service;

import com.nacho.blog.spring.integration.tests.demo.model.LogEntry;
import com.nacho.blog.spring.integration.tests.demo.model.Shipment;
import com.nacho.blog.spring.integration.tests.demo.model.User;
import com.nacho.blog.spring.integration.tests.demo.repository.LogRepository;
import com.nacho.blog.spring.integration.tests.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import retrofit2.Response;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

  @Autowired
  private final UserRepository userRepository;

  @Autowired
  private final UserApi userApi;

  @Autowired
  private final LogRepository logRepository;

  public User getById(final Integer userId) {
    log.info("Searching user {} in the database", userId);
    return userRepository.findById(userId)
            .map(user -> {
              log.info("User found in the DB, no need to query the api");
              return user;
            }).orElseGet(() -> getUserFromApi(userId));
  }

  private User getUserFromApi(final Integer userId) {
    log.info("User {} not found in the database, falling back to the api", userId);
    try {
      final Response<User> userResponse = userApi.getById(userId).execute();
      if (userResponse.isSuccessful()) {
        final User user = userResponse.body();
        log.info("Found user in the api, saving into the DB");
        User savedUser = userRepository.save(user);
        logRepository.save(new LogEntry(LogEntry.LogEntryType.CREATE, User.class.getName(),
                user.getId().toString(), LocalDateTime.now()));
        return savedUser;
      }
      log.error("Error fetching user {}", userId);
      throw new RuntimeException();
    } catch (final Exception e) {
      log.error("Error fetching user {}. Exception: {}", userId, e);
      throw new RuntimeException(e);
    }
  }
}
