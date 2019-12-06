package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.LoginUser;
import com.groudina.ten.demo.dto.NewUser;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.jwt.JWTProvider;
import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.mongo.embedded.EmbeddedMongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableEmbeddedMongo
//@ActiveProfiles("test")
class AuthControllerTest {

    @Configuration
    class MongoConfig {

    }

    WebTestClient webTestClient;

    @Autowired
    AuthController controller;

    @Autowired
    DbUserRepository userRepository;

    @Autowired
    DbRolesRepository rolesRepository;

    @Autowired
    PasswordEncoder encoder;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToController(controller).build();
        rolesRepository.saveAll(List.of(DbRole.builder().name("ROLE_STUDENT").build(),
                DbRole.builder().name("ROLE_TEACHER").build(),
                DbRole.builder().name("ROLE_ADMIN").build())).blockLast();
    }

    @AfterEach
    void tearDown() {
        rolesRepository.deleteAll().block();
        userRepository.deleteAll().block();
    }

    @Test
    void authenticateUser() {
        final String password = "1234";
        userRepository.save(DbUser.builder().email("email").password(encoder.encode(password)).build()).block();

        webTestClient.post().uri("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(LoginUser.builder().email("email").password(password).build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseMessage.class);
    }

    @Test
    void registerUser() {
        final String password = "1234";
        final String email = "email";

        webTestClient.post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewUser.builder().email(email).password(password).build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseMessage.class).consumeWith(message -> {
                    assertEquals(userRepository.count().block(), 1);
        });
    }
}