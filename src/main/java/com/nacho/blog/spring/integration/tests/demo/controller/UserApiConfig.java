package com.nacho.blog.spring.integration.tests.demo.controller;

import com.nacho.blog.spring.integration.tests.demo.service.UserApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Configuration
public class UserApiConfig {

  @Bean
  public UserApi userApi(@Value("${userapi.url}") String userApiUrl){
    return new Retrofit.Builder() //
            .baseUrl(userApiUrl) //
            .addConverterFactory(GsonConverterFactory.create()) //
            .build()
            .create(UserApi.class);
  }
}
