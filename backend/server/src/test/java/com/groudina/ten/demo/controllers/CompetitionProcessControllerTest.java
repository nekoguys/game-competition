package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.*;
import com.groudina.ten.demo.dto.CompetitionAnswerRequestDto;
import com.groudina.ten.demo.dto.CompetitionMessageRequest;
import com.groudina.ten.demo.dto.ResponseMessage;
import com.groudina.ten.demo.dto.RoundTeamAnswerDto;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import com.groudina.ten.demo.services.IGameManagementService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.springSecurity;

@SpringBootTest
@EnableEmbeddedMongo
class CompetitionProcessControllerTest {

    @Autowired
    ApplicationContext context;

    @Configuration
    class MongoConfig {}

    WebTestClient webTestClient;

    @Autowired
    CompetitionProcessController competitionProcessController;

    @Autowired
    IGameManagementService gameManagementService;

    @Autowired
    DbRolesRepository rolesRepository;

    @Autowired
    DbUserRepository userRepository;

    @Autowired
    DbCompetitionsRepository competitionsRepository;

    @Autowired
    DbTeamsRepository teamsRepository;

    @Autowired
    DbAnswersRepository answersRepository;

    @Autowired
    DbCompetitionMessagesRepository messagesRepository;

    @Autowired
    DbCompetitionProcessInfosRepository processInfosRepository;

    @Autowired
    DbCompetitionRoundInfosRepository roundInfosRepository;

    @Autowired
    DbRoundResultElementsRepository roundResultElementsRepository;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToApplicationContext(this.context).apply(springSecurity()).configureClient().build().mutate().responseTimeout(Duration.ofSeconds(10000)).build();
        rolesRepository.saveAll(List.of(DbRole.builder().name("ROLE_STUDENT").build(),
                DbRole.builder().name("ROLE_TEACHER").build(),
                DbRole.builder().name("ROLE_ADMIN").build())).blockLast();
    }

    @AfterEach
    void tearDown() {
        rolesRepository.deleteAll().block();
        userRepository.deleteAll().block();
        competitionsRepository.deleteAll().block();
        teamsRepository.deleteAll().block();
        answersRepository.deleteAll().block();
        messagesRepository.deleteAll().block();
        processInfosRepository.deleteAll().block();
        roundInfosRepository.deleteAll().block();
        roundResultElementsRepository.deleteAll().block();
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    void testStartCompetition() {
        var owner = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();

        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder().build())
                .owner(owner)
                .build()).block();

        webTestClient.get().uri("/api/competition_process/1234/start_competition")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ResponseMessage.class)
                .value(responseMessage -> {
                    assertTrue(responseMessage.getMessage().contains("started"));
                });

        var comp = competitionsRepository.findByPin("1234").block();
        assertEquals(comp.getState(), DbCompetition.State.InProcess);
        assertNotNull(comp.getCompetitionProcessInfo());
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    void testStartCompetitionNotFound() {
        var owner = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();

        webTestClient.get().uri("/api/competition_process/pinpin/start_competition")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ResponseMessage.class)
                .value(responseMessage -> {
                    assertTrue(responseMessage.getMessage().contains("not found"));
                });
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    void testStartEndRound() {
        var owner = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();

        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder().roundsCount(2).build())
                .owner(owner)
                .build()).block();

        webTestClient.get().uri("/api/competition_process/1234/start_competition")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ResponseMessage.class)
                .value(responseMessage -> {
                    assertTrue(responseMessage.getMessage().contains("started"));
                });

        var comp = competitionsRepository.findByPin("1234").block();
        assertEquals(comp.getState(), DbCompetition.State.InProcess);
        assertNotNull(comp.getCompetitionProcessInfo());

        webTestClient.get().uri("/api/competition_process/1234/end_round")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ResponseMessage.class)
                .value(responseMessage -> assertTrue(responseMessage.getMessage().contains("success")));

        comp = competitionsRepository.findByPin("1234").block();

        assertTrue(comp.getCompetitionProcessInfo().getCurrentRound().isEnded());

        webTestClient.get().uri("/api/competition_process/1234/start_round")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ResponseMessage.class)
                .value(responseMessage -> {
                    assertTrue(responseMessage.getMessage().contains("success"));
                });

        comp = competitionsRepository.findByPin("1234").block();
        assertEquals(comp.getCompetitionProcessInfo().getCurrentRoundNumber(), 2);
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER"})
    void testSendMessage() {
        var owner = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();

        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder().roundsCount(2).build())
                .owner(owner)
                .build()).block();
        gameManagementService.startCompetition(competition).block();

        webTestClient.post().uri("/api/competition_process/1234/send_message")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(CompetitionMessageRequest.builder().message("message").build()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ResponseMessage.class)
                .value(responseMessage -> assertTrue(responseMessage.getMessage().contains("success")));
        var comp = competitionsRepository.findByPin("1234").block();

        assertEquals(comp.getCompetitionProcessInfo().getMessages().size(), 1);
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    void testSubmitAnswer() {
        var captain = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder().roundsCount(2).build())
                .build()).block();
        var team = teamsRepository.save(DbTeam.builder().captain(captain).name("abac").sourceCompetition(competition).build()).block();
        competition.addTeam(team);
        competition = competitionsRepository.save(competition).block();

        gameManagementService.startCompetition(competition).block();

        webTestClient.post().uri("/api/competition_process/1234/submit_answer")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(CompetitionAnswerRequestDto.builder().answer(10).roundNumber(1).teamName("abac").build()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ResponseMessage.class)
                .value(responseMessage -> assertTrue(responseMessage.getMessage().contains("success")));

        Flux<RoundTeamAnswerDto> answers = webTestClient.get().uri("/api/competition_process/1234/answers_stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(RoundTeamAnswerDto.class).getResponseBody();


        StepVerifier.create(answers).consumeNextWith(roundTeamAnswerDto -> {
            assertEquals(roundTeamAnswerDto.getRoundNumber(), 1);
            assertEquals(roundTeamAnswerDto.getTeamIdInGame(), 0);
            assertEquals(roundTeamAnswerDto.getTeamAnswer(), 10);
        }).thenCancel().verify();

        var comp = competitionsRepository.findByPin("1234").block();
        assertEquals(comp.getCompetitionProcessInfo().getCurrentRound().getAnswerList().size(), 1);
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    void testSubmitAnswerFailure() {
        var user = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var captain = userRepository.save(DbUser.builder().password("1234").email("anotherEmail").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder().roundsCount(2).build())
                .build()).block();
        var team = teamsRepository.save(DbTeam.builder().captain(captain).name("abac").sourceCompetition(competition).build()).block();
        competition.addTeam(team);
        competition = competitionsRepository.save(competition).block();

        gameManagementService.startCompetition(competition).block();

        webTestClient.post().uri("/api/competition_process/1234/submit_answer")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(CompetitionAnswerRequestDto.builder().answer(10).roundNumber(1).teamName("abac").build()))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ResponseMessage.class)
                .value(ex -> assertTrue(ex.getMessage().matches("(.*)is not team (.*) captain(.*)")));
        var comp = competitionsRepository.findByPin("1234").block();
        assertEquals(comp.getCompetitionProcessInfo().getCurrentRound().getAnswerList().size(), 0);
    }
}