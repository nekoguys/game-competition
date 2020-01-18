package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbRolesRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.NewCompetition;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.jwt.UserDetailsServiceImpl;
import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.*;

@SpringBootTest
@EnableEmbeddedMongo
class CompetitionsControllerTest {

    @Autowired
    ApplicationContext context;

    @Configuration
    class MongoConfig {}

    WebTestClient webTestClient;

    @Autowired
    CompetitionsController controller;

    @Autowired
    DbUserRepository userRepository;

    @Autowired
    DbRolesRepository rolesRepository;

    @Autowired
    DbCompetitionsRepository competitionsRepository;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToApplicationContext(this.context).apply(springSecurity()).configureClient().build();
        rolesRepository.saveAll(List.of(DbRole.builder().name("ROLE_STUDENT").build(),
                DbRole.builder().name("ROLE_TEACHER").build(),
                DbRole.builder().name("ROLE_ADMIN").build())).blockLast();
    }

    @AfterEach
    void tearDown() {
        rolesRepository.deleteAll().block();
        userRepository.deleteAll().block();
        competitionsRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(roles = {"STUDENT"})
    void createCompetitionForbidden() {
        webTestClient.post().uri("/api/competitions/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewCompetition.builder()
                        .shouldShowStudentPreviousRoundResults(false)
                        .state("draft")
                        .maxTeamsAmount(1)
                        .demandFormula(List.of("1", "2"))
                        .expensesFormula(List.of("1", "2", "3"))
                        .instruction("instr")
                        .name("name")
                        .roundLength(1)
                        .roundsCount(2)
                        .shouldEndRoundBeforeAllAnswered(false)
                        .shouldShowResultTableInEnd(false)
                        .maxTeamSize(3)
                        .build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isForbidden();
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    void createCompetition() {
        var owner = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collect(Collectors.toList()).block()).build()).block();
        assertEquals(0, competitionsRepository.findAll().count().block());

        webTestClient.post().uri("/api/competitions/create")
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(NewCompetition.builder()
                        .shouldShowStudentPreviousRoundResults(false)
                        .state("draft")
                        .maxTeamsAmount(1)
                        .demandFormula(List.of("1", "2"))
                        .expensesFormula(List.of("1", "2", "3"))
                        .instruction("instr")
                        .name("name")
                        .roundLength(1)
                        .roundsCount(2)
                        .shouldEndRoundBeforeAllAnswered(false)
                        .shouldShowResultTableInEnd(false)
                        .maxTeamSize(3)
                        .build()))
                .accept(MediaType.APPLICATION_JSON)
                .exchange().expectStatus().isOk()
                .expectBody(ResponseMessage.class)
        ;
        assertEquals(1, competitionsRepository.findAll().count().block());
        assertEquals(competitionsRepository.findAll().collect(Collectors.toList()).block().get(0).getOwner(), owner);
    }
}