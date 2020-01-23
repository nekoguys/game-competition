package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.RolePostRequest;
import com.groudina.ten.demo.dto.RoleResponse;
import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@EnableEmbeddedMongo
class RolesControllerTest {

    WebTestClient webTestClient;
    @Autowired
    RolesController controller;
    @Autowired
    DbUserRepository userRepository;
    @Autowired
    DbRolesRepository rolesRepository;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(controller).build();
        rolesRepository.saveAll(List.of(DbRole.builder().name("ROLE_STUDENT").build(),
                DbRole.builder().name("ROLE_TEACHER").build(),
                DbRole.builder().name("ROLE_ADMIN").build())).blockLast();

        userRepository.save(DbUser.builder()
                .password("1234")
                .email("target@hse.ru")
                .roles(rolesRepository
                        .findByName("ROLE_STUDENT")
                        .flux()
                        .collectList()
                        .block())
                .build()
        ).block();

        userRepository.save(DbUser.builder()
                .password("1234")
                .email("admin@hse.ru")
                .roles(rolesRepository
                        .findAll()
                        .collectList()
                        .block())
                .build()
        ).block();
    }

    @AfterEach
    void tearDown() {
        rolesRepository.deleteAll().block();
        userRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(value = "target@hse.ru", password = "1234", roles = {"STUDENT"})
    void putRolesForbidden() {
        webTestClient.post().uri("/api/roles/target@hse.ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new RolePostRequest("ROLE_ADMIN")))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(value = "admin@hse.ru", password = "1234", roles = {"ADMIN"})
    void postRoles() {
        webTestClient.post().uri("/api/roles/target@hse.ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new RolePostRequest("ROLE_ADMIN")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(RoleResponse.class)
                .value(response -> {
                    assertEquals("ROLE_ADMIN", response.getRole());
                });

        var admin = userRepository.findOneByEmail("target@hse.ru").block();
        assertTrue(admin.getRoles().stream().anyMatch(x -> x.getName().equals("ROLE_ADMIN")));
    }

    @Test
    @WithMockUser(value = "admin@hse.ru", password = "1234", roles = {"ADMIN"})
    void postRolesFoolProof() {
        webTestClient.post().uri("/api/roles/admin@hse.ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new RolePostRequest("ROLE_STUDENT")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(value = "admin@hse.ru", password = "1234", roles = {"ADMIN"})
    void postRolesWrongEmail() {
        webTestClient.post().uri("/api/roles/accidentally_wrong_email@hse.ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new RolePostRequest("ROLE_ADMIN")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(value = "admin@hse.ru", password = "1234", roles = {"ADMIN"})
    void postRolesWrongRole() {
        webTestClient.post().uri("/api/roles/target@hse.ru")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(new RolePostRequest("ROLE_GOD (it's funny because ROLE_GOD doesn't exist")))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    @WithMockUser(value = "student@hse.ru", password = "1234", roles = {"STUDENT"})
    void getRoles() {
        webTestClient.get().uri("/api/roles/admin@hse.ru")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(RoleResponse.class)
                .value(response -> {
                    assertEquals("ROLE_ADMIN", response.getRole());
                });
    }

    @Test
    @WithMockUser(value = "student@hse.ru", password = "1234", roles = {"STUDENT"})
    void getRolesWrongEmail() {
        webTestClient.get().uri("/api/roles/hello@hse.ru")
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isBadRequest();
    }

    @Configuration
    class MongoConfig {
    }
}