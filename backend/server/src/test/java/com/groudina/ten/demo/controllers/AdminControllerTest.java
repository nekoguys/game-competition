package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.UserSearchRequest;
import com.groudina.ten.demo.dto.UserSearchResponse;
import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest
@EnableEmbeddedMongo
public class AdminControllerTest {
    @Autowired
    ApplicationContext context;

    @Autowired
    AdminController controller;

    @Autowired
    DbUserRepository userRepository;

    @Autowired
    DbRolesRepository rolesRepository;

    WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToApplicationContext(this.context).apply(springSecurity()).configureClient().build();
        rolesRepository.saveAll(List.of(DbRole.builder().name("ROLE_STUDENT").build(),
                DbRole.builder().name("ROLE_TEACHER").build(),
                DbRole.builder().name("ROLE_ADMIN").build())).blockLast();
        var roles = rolesRepository.findAll().collectList().block();
        var admin = DbUser.builder()
                .password("1234")
                .email("test@hse.ru")
                .roles(roles)
                .profile(new DbUser.Profile("Иван", "Petrov"))
                .build();
        var teacher = DbUser.builder()
                .password("1234")
                .email("email@hse.ru")
                .roles(roles.stream()
                        .filter(x -> !x.getName().contains("ADMIN"))
                        .collect(Collectors.toList()))
                .profile(new DbUser.Profile("Petr", "Tarzan"))
                .build();
        var student = DbUser.builder()
                .password("1234")
                .email("e@edu.hse.ru")
                .roles(roles.stream()
                        .filter(x -> x.getName().contains("STUDENT"))
                        .collect(Collectors.toList()))
                .profile(new DbUser.Profile("Tanya", "Gaiduk"))
                .build();
        userRepository.saveAll(List.of(admin, teacher, student)).blockLast();
    }

    @AfterEach
    void tearDown() {
        rolesRepository.deleteAll().block();
        userRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(username = "gaiduk@hse.ru", password = "1234", roles = {"STUDENT"})
    void adminkaForbidden() {
        var request = UserSearchRequest.builder()
                .query("literally anything")
                .build();

        webTestClient.post().uri("/api/admin/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .exchange()
                .expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(value = "teacher@hse.ru", password = "1234", roles = {"TEACHER"})
    void searchWithNoResults() {
        var request = UserSearchRequest.builder()
                .query("literally anything")
                .page(0)
                .pageSize(10)
                .build();

        webTestClient.post().uri("/api/admin/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserSearchResponse.class)
                .value(response -> {
                    var results = response.getResults();
                    assertEquals(0, results.size());
                });
    }

    @Test
    @WithMockUser(value = "teacher@hse.ru", password = "1234", roles = {"TEACHER"})
    void incorrectPattern() {
        var request = UserSearchRequest.builder()
                .query("(")
                .page(0)
                .pageSize(10)
                .build();

        webTestClient.post().uri("/api/admin/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserSearchResponse.class)
                .value(response -> {
                    var results = response.getResults();
                    assertEquals(0, results.size());
                });
    }

    @Test
    @WithMockUser(value = "teacher@hse.ru", password = "1234", roles = {"TEACHER"})
    void searchForT() {
        var request = UserSearchRequest.builder()
                .query("T")
                .page(0)
                .pageSize(10)
                .build();

        webTestClient.post().uri("/api/admin/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserSearchResponse.class)
                .value(response -> {
                    var results = response.getResults();
                    // у админа почта начинается на t, у учителя - фамилия, у студента - имя. 3 результата
                    assertEquals(3, results.stream().map(x -> x.getEmail()).distinct().count());
                });
    }

    @Test
    @WithMockUser(value = "teacher@hse.ru", password = "1234", roles = {"TEACHER"})
    void searchForEmail() {
        var request = UserSearchRequest.builder()
                .query("e")
                .page(0)
                .pageSize(10)
                .build();

        webTestClient.post().uri("/api/admin/search")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(request))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserSearchResponse.class)
                .value(response -> {
                    var results = response.getResults();
                    // у админа почта начинается на t, у учителя - фамилия, у студента - имя. 3 результата
                    assertEquals(2, results.size());
                    assertTrue(results.stream().anyMatch(x -> x.getEmail().equals("email@hse.ru")));
                    assertTrue(results.stream().anyMatch(x -> x.getEmail().equals("e@edu.hse.ru")));
                });
    }
}
