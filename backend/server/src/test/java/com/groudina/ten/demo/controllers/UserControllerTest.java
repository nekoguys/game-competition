package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.NewUserWithRole;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.models.DbRole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest
@EnableEmbeddedMongo
class UserControllerTest {

    @Configuration
    class MongoConfig {}

    WebTestClient webTestClient;

    @Autowired
    ApplicationContext context;

    @Autowired
    UserController controller;

    @Autowired
    DbUserRepository userRepository;

    @Autowired
    DbRolesRepository rolesRepository;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToApplicationContext(this.context).apply(springSecurity()).configureClient().build();
        rolesRepository.saveAll(List.of(DbRole.builder().name("ROLE_STUDENT").build(),
                DbRole.builder().name("ROLE_TEACHER").build(),
                DbRole.builder().name("ROLE_ADMIN").build())).blockLast();
    }

    @AfterEach
    void tearDown() {
        userRepository.deleteAll().block();
        rolesRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createAdminUser() {
        NewUserWithRole user = NewUserWithRole.builder()
                .email("email")
                .password(encoder.encode("password"))
                .role("ROLE_ADMIN").build();

        webTestClient.post().uri("api/users/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(user))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseMessage.class)
                .value(resp -> assertEquals(resp.getMessage(), "User created successfully!"));
    }
}