package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.datasource.DbVerificationTokenRepository;
import com.groudina.ten.demo.dto.LoginUser;
import com.groudina.ten.demo.dto.NewUser;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.services.IEmailService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableEmbeddedMongo
class AuthControllerTest {

    @Configuration
    class MongoConfig {}

    WebTestClient webTestClient;

    @Autowired
    AuthController controller;

    @Autowired
    DbUserRepository userRepository;

    @Autowired
    DbRolesRepository rolesRepository;

    @Autowired
    DbVerificationTokenRepository verificationTokenRepository;

    @Autowired
    PasswordEncoder encoder;

    @MockBean
    IEmailService emailService;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToController(controller).build();
        rolesRepository.saveAll(List.of(DbRole.builder().name("ROLE_STUDENT").build(),
                DbRole.builder().name("ROLE_TEACHER").build(),
                DbRole.builder().name("ROLE_ADMIN").build())).blockLast();
        Mockito.when(emailService.sendEmail(Mockito.anyString(), Mockito.anyString())).thenReturn(Mono.empty());
    }

    @AfterEach
    void tearDown() {
        rolesRepository.deleteAll().block();
        userRepository.deleteAll().block();
        verificationTokenRepository.deleteAll().block();
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
    void authenticateUserBadCredentials() {
        final String password = "1234";
        userRepository.save(DbUser.builder().email("email@edu.hse.ru").password(encoder.encode(password)).build()).block();

        webTestClient.post().uri("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(LoginUser.builder().email("email@edu.hse.ru").password(password + "2").build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class)
                .value((respMessage) -> {
                    assertTrue(respMessage.getMessage().contains("Invalid"));
                    assertTrue(respMessage.getMessage().contains("credentials"));
                });
    }

    @Test
    void authenticateUserNoSuchUser() {
        final String password = "1234";
        userRepository.save(DbUser.builder().email("email@edu.hse.ru").password(encoder.encode(password)).build()).block();

        webTestClient.post().uri("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(LoginUser.builder().email("email1@edu.hse.ru").password(password).build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class)
                .value((respMessage) -> {
                    assertTrue(respMessage.getMessage().contains("No user with email"));
                });
    }

    @Test
    void registerUser() {
        final String password = "1234";
        final String email = "email@edu.hse.ru";

        webTestClient.post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewUser.builder().email(email).password(password).build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ResponseMessage.class).consumeWith(message -> assertEquals(userRepository.count().block(), 1));
    }

    @Test
    void registerUserRepeatingEmail() {
        final String password = "1234";
        final String email = "email@edu.hse.ru";

        userRepository.save(DbUser.builder().email(email).password(encoder.encode(password)).build()).block();

        webTestClient.post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewUser.builder().email(email).password(password).build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).value(respMes -> {
                    assertTrue(respMes.getMessage().contains("User with email"));
                    assertTrue(respMes.getMessage().contains("already exists"));
        });
    }

    @Test
    void registerUserWithWrongDomainEmail() {
        final String password = "1234";
        final String email = "email";

        userRepository.save(DbUser.builder().email(email).password(encoder.encode(password)).build()).block();

        webTestClient.post().uri("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewUser.builder().email(email).password(password).build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(ResponseMessage.class).value(respMes -> assertTrue(respMes.getMessage().contains("Invalid email")));
    }

    @Test
    void testVerification() {
        webTestClient.post().uri("/api/auth/signup")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewUser.builder().email("email@edu.hse.ru").password("1234").build()))
                .exchange()
                .expectStatus().isOk();

        var user = userRepository.findAll().blockFirst();
        assertFalse(user.isVerified());

        var token = verificationTokenRepository.findAll().blockFirst();
        assertNotNull(token);
        assertEquals(token.getUser().getEmail(), "email@edu.hse.ru");

        webTestClient.get().uri("/api/auth/verification/" + token.getToken())
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        user = userRepository.findAll().blockFirst();
        assertTrue(user.isVerified());
    }
}