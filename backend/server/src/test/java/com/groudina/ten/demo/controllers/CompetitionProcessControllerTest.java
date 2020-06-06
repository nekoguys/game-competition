package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.*;
import com.groudina.ten.demo.dto.*;
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
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;

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
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

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
        gameManagementService.clear().block();
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
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER", "STUDENT"})
    void testStartEndRound() {
        var owner = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();

        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder().roundsCount(2)
                        .expensesFormula(List.of("1", "2", "3"))
                        .demandFormula(List.of("1", "2"))
                        .build())
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

        webTestClient.get().uri("/api/competition_process/1234/start_round")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ResponseMessage.class)
                .value(responseMessage -> assertTrue(responseMessage.getMessage().contains("success")));

        var roundStream = webTestClient.get().uri("/api/competition_process/1234/rounds_stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(ITypedEvent.class).getResponseBody();

        var verifier = StepVerifier.create(roundStream)
                .consumeNextWith(event -> {
                    assertEquals(event.getType(), "NewRound");
                    NewRoundEventDto roundEventDto = (NewRoundEventDto)event;
                    assertEquals(roundEventDto.getRoundNumber(), 1);
                    assertEquals(roundEventDto.getRoundLength(), 0);
                    assertTrue(Math.abs(roundEventDto.getBeginTime() - LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond()) <= 100);
                }).consumeNextWith(event -> {
                    assertEquals(event.getType(), "EndRound");
                    EndRoundEventDto endRoundEventDto = (EndRoundEventDto)event;
                    assertFalse(endRoundEventDto.isEndOfGame());
                    assertEquals(endRoundEventDto.getRoundNumber(), 1);
                }).consumeNextWith(event -> {
                    NewRoundEventDto roundEventDto = (NewRoundEventDto)event;
                    assertEquals(roundEventDto.getRoundNumber(), 2);
                    assertEquals(roundEventDto.getRoundLength(), 0);
                    assertTrue(Math.abs(roundEventDto.getBeginTime() - LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond()) <= 100);
                }).thenCancel().verifyLater();

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
        verifier.verify();
        comp = competitionsRepository.findByPin("1234").block();
        assertEquals(comp.getCompetitionProcessInfo().getCurrentRoundNumber(), 2);
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER", "STUDENT"})
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

        var messages = webTestClient.get().uri("/api/competition_process/1234/messages_stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus()
                .isOk()
                .returnResult(CompetitionMessageDto.class).getResponseBody();

        StepVerifier.create(messages).consumeNextWith(message -> {
            assertEquals(message.getMessage(), "message");
            assertTrue(Math.abs(message.getSendTime() - LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond()) <= 1);
        }).thenCancel().verify();

        var comp = competitionsRepository.findByPin("1234").block();

        assertEquals(comp.getCompetitionProcessInfo().getMessages().size(), 1);
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT", "TEACHER"})
    void testSubmitAnswer() {
        var captain = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder().roundsCount(2).demandFormula(List.of("1", "2")).expensesFormula(List.of("1","2", "3")).build())
                .build()).block();
        var team = teamsRepository.save(DbTeam.builder().captain(captain).name("abac").sourceCompetition(competition).build()).block();
        competition.addTeam(team);
        competition = competitionsRepository.save(competition).block();

        gameManagementService.startCompetition(competition).block();
        gameManagementService.startNewRound(competition).block();

        webTestClient.post().uri("/api/competition_process/1234/submit_answer")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(CompetitionAnswerRequestDto.builder().answer(10).roundNumber(1).build()))
                .exchange()
                .expectStatus()
                .isOk()
                .expectBody(ResponseMessage.class)
                .value(responseMessage -> assertTrue(responseMessage.getMessage().contains("success")));

        competition =  competitionsRepository.findByPin("1234").block();

        gameManagementService.endCurrentRound(competition).block();
        gameManagementService.startNewRound(competition).block();

        webTestClient.post().uri("/api/competition_process/1234/submit_answer")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(CompetitionAnswerRequestDto.builder().answer(20).roundNumber(2).build()))
                .exchange()
                .expectStatus().isOk()
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
        }).consumeNextWith(roundTeamAnswerDto -> {
            assertEquals(roundTeamAnswerDto.getTeamAnswer(), 20);
            assertEquals(roundTeamAnswerDto.getTeamIdInGame(), 0);
            assertEquals(roundTeamAnswerDto.getRoundNumber(), 2);
        }).thenCancel().verify();

        var comp = competitionsRepository.findByPin("1234").block();
        assertEquals(comp.getCompetitionProcessInfo().getCurrentRound().getAnswerList().size(), 1);
        assertEquals(comp.getCompetitionProcessInfo().getRoundInfos().get(0).getAnswerList().size(), 1);
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
        var team = teamsRepository.save(DbTeam.builder().captain(captain).name("abac").allPlayers(List.of(user)).sourceCompetition(competition).build()).block();
        competition.addTeam(team);
        competition = competitionsRepository.save(competition).block();

        gameManagementService.startCompetition(competition).block();
        gameManagementService.startNewRound(competition).block();


        webTestClient.post().uri("/api/competition_process/1234/submit_answer")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(CompetitionAnswerRequestDto.builder().answer(10).roundNumber(1).build()))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ResponseMessage.class)
                .value(ex -> assertTrue(ex.getMessage().matches("(.*)is not team (.*) captain(.*)")));
        var comp = competitionsRepository.findByPin("1234").block();
        assertEquals(comp.getCompetitionProcessInfo().getCurrentRound().getAnswerList().size(), 0);
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"STUDENT"})
    void testSubmitAnswerOutOfRange() {
        var user = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder().roundsCount(2).build())
                .build()).block();
        var team = teamsRepository.save(DbTeam.builder().captain(user).name("abac").allPlayers(List.of(user)).sourceCompetition(competition).build()).block();
        competition.addTeam(team);
        competition = competitionsRepository.save(competition).block();

        gameManagementService.startCompetition(competition).block();
        gameManagementService.startNewRound(competition).block();

        webTestClient.post().uri("/api/competition_process/1234/submit_answer")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(CompetitionAnswerRequestDto.builder().answer(0).roundNumber(1).build()))
                .exchange()
                .expectStatus()
                .isBadRequest()
                .expectBody(ResponseMessage.class)
                .value(ex -> assertTrue(ex.getMessage().matches("(.*)too small (.*) too big(.*)")));
        var comp = competitionsRepository.findByPin("1234").block();
        assertEquals(comp.getCompetitionProcessInfo().getCurrentRound().getAnswerList().size(), 0);
    }

    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER","STUDENT"})
    void testResultsEvents() {
        var user = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var captain = userRepository.save(DbUser.builder().password("1234").email("anotherEmail").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder()
                        .roundsCount(2)
                        .expensesFormula(List.of("1", "2", "3"))
                        .demandFormula(List.of("100", "10"))
                        .build())
                .build()).block();
        var team = teamsRepository.save(DbTeam.builder().captain(captain).idInGame(0).name("abac").sourceCompetition(competition).build()).block();
        var team2 = teamsRepository.save(DbTeam.builder().captain(captain).idInGame(1).name("kuks").sourceCompetition(competition).build()).block();
        competition.addTeam(team);
        competition.addTeam(team2);
        competition = competitionsRepository.save(competition).block();

        gameManagementService.startCompetition(competition).block();
        gameManagementService.startNewRound(competition).block();


        gameManagementService.submitAnswer(competition, team, 10, 1).block();
        gameManagementService.submitAnswer(competition, team2, 30, 1).block();

        gameManagementService.endCurrentRound(competition).block();

        var results = webTestClient.get().uri("/api/competition_process/1234/results_stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(RoundTeamResultDto.class).getResponseBody();

        StepVerifier.create(results)
                .consumeNextWith(roundTeamResultDto -> {
                    assertEquals(roundTeamResultDto.getTeamIdInGame(), 0);
                    assertEquals(roundTeamResultDto.getIncome(), -63.0, 0.001);
                    assertEquals(roundTeamResultDto.getRoundNumber(), 1);
                }).expectNextCount(1).thenCancel().verify();

    }


    @Test
    @WithMockUser(value = "email", password = "1234", roles = {"TEACHER","STUDENT"})
    void testPricesEvents() {
        var user = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var captain = userRepository.save(DbUser.builder().password("1234").email("anotherEmail").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder()
                        .roundsCount(2)
                        .expensesFormula(List.of("1", "2", "3"))
                        .demandFormula(List.of("100", "10"))
                        .build())
                .build()).block();
        var team = teamsRepository.save(DbTeam.builder().captain(captain).idInGame(0).name("abac").sourceCompetition(competition).build()).block();
        var team2 = teamsRepository.save(DbTeam.builder().captain(captain).idInGame(1).name("kuks").sourceCompetition(competition).build()).block();
        competition.addTeam(team);
        competition.addTeam(team2);
        competition = competitionsRepository.save(competition).block();

        gameManagementService.startCompetition(competition).block();
        gameManagementService.startNewRound(competition).block();

        gameManagementService.submitAnswer(competition, team, 10, 1).block();
        gameManagementService.submitAnswer(competition, team2, 30, 1).block();

        gameManagementService.endCurrentRound(competition).block();

        var results = webTestClient.get().uri("/api/competition_process/1234/prices_stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(PriceInRoundDto.class).getResponseBody();

        StepVerifier.create(results)
                .consumeNextWith(roundTeamResultDto -> {
                    assertEquals(roundTeamResultDto.getPrice(), 6.0, 0.001);
                    assertEquals(roundTeamResultDto.getRoundNumber(), 1);
                }).thenCancel().verify();
    }

    @Test
    @WithMockUser(value = "anotherEmail", password = "1234", roles = {"TEACHER","STUDENT"})
    void testRoundInfoForResultsTable() {
        var captain = userRepository.save(DbUser.builder().password("1234").email("anotherEmail").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder()
                        .roundsCount(3)
                        .name("name")
                        .expensesFormula(List.of("1", "2", "3"))
                        .demandFormula(List.of("100", "10"))
                        .maxTeamsAmount(100)
                        .build())
                .build()).block();
        var team = teamsRepository.save(DbTeam.builder().captain(captain).idInGame(0).name("abac").sourceCompetition(competition).build()).block();
        var team2 = teamsRepository.save(DbTeam.builder().captain(captain).idInGame(1).name("kuks").sourceCompetition(competition).build()).block();
        competition.addTeam(team);
        competition.addTeam(team2);
        competition = competitionsRepository.save(competition).block();

        webTestClient.get().uri("/api/competition_process/1234/comp_info")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CompetitionInfoForResultsTableDto.class)
                .value(el -> {
                    assertEquals(el.getConnectedTeamsCount(), 2);
                    assertEquals(el.getName(), "name");
                    assertEquals(el.getRoundsCount(), 3);
                });
    }

    @Test
    @WithMockUser(value = "anotherEmail", password = "1234", roles = {"TEACHER","STUDENT"})
    void testStudentCompetitionInfo() {
        var user = userRepository.save(DbUser.builder().password("1234").email("anotherEmail").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder()
                        .roundsCount(3)
                        .name("name")
                        .expensesFormula(List.of("1", "2", "3"))
                        .demandFormula(List.of("100", "10"))
                        .maxTeamsAmount(100)
                        .shouldShowStudentPreviousRoundResults(true)
                        .instruction("instr")
                        .build())
                .build()).block();
        competition = competitionsRepository.save(competition).block();
        var team = teamsRepository.save(DbTeam.builder().sourceCompetition(competition).name("tutu").idInGame(0).captain(user).build()).block();
        competition.addTeam(team);
        competition = competitionsRepository.save(competition).block();

        webTestClient.get().uri("/api/competition_process/1234/student_comp_info")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CompetitionInfoForStudentResultsTableDto.class).value(el -> {
                    assertTrue(el.isCaptain());
                    assertEquals(el.getDescription(), "instr");
                    assertEquals(el.getName(), "name");
                    assertEquals(el.getRoundsCount(), 3);
                    assertTrue(el.isShouldShowResultTable());
                    assertEquals(el.getTeamName(), "tutu");
        });
    }

    @Test
    @WithMockUser(value = "anotherEmail", password = "1234", roles = {"TEACHER","STUDENT"})
    void testMyTeamAnswerStream() {
        var user = userRepository.save(DbUser.builder().password("1234").email("anotherEmail").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var secUser = userRepository.save(DbUser.builder().password("1234").email("email").roles(rolesRepository.findAll().collectList().block()).build()).block();
        var competition = competitionsRepository.save(DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .parameters(DbCompetition.Parameters.builder()
                        .roundsCount(3)
                        .name("name")
                        .expensesFormula(List.of("1", "2", "3"))
                        .demandFormula(List.of("100", "10"))
                        .maxTeamsAmount(100)
                        .shouldShowStudentPreviousRoundResults(true)
                        .instruction("instr")
                        .build())
                .build()).block();
        competition = competitionsRepository.save(competition).block();
        var team = teamsRepository.save(DbTeam.builder().sourceCompetition(competition).name("tutu").idInGame(0).captain(user).build()).block();
        var team2 = teamsRepository.save(DbTeam.builder().sourceCompetition(competition).name("abab").idInGame(1).captain(secUser).build()).block();

        competition.addTeam(team);
        competition.addTeam(team2);

        gameManagementService.startCompetition(competition).block();
        gameManagementService.startNewRound(competition).block();
        gameManagementService.submitAnswer(competition, team, 10, 1).block();
        gameManagementService.submitAnswer(competition, team2, 20, 1).block();

        var answersFlux = webTestClient.get().uri("/api/competition_process/1234/my_answers_stream")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(RoundTeamAnswerDto.class)
                .getResponseBody();
        var verifier = StepVerifier.create(answersFlux).consumeNextWith(el -> {
            assertEquals(el.getRoundNumber(), 1);
            assertEquals(el.getTeamAnswer(), 10);
            assertEquals(el.getTeamIdInGame(), 0);
        }).consumeNextWith(el -> {
            assertEquals(el.getRoundNumber(), 2);
            assertEquals(el.getTeamAnswer(), 20);
            assertEquals(el.getTeamIdInGame(), 0);
        }).thenCancel().verifyLater();

        gameManagementService.endCurrentRound(competition).block();
        gameManagementService.startNewRound(competition).block();

        gameManagementService.submitAnswer(competition, team, 20, 2).block();
        verifier.verify();
    }
}