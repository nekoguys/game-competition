package com.groudina.ten.demo.services;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.*;
import com.groudina.ten.demo.dto.CompetitionMessageRequest;
import com.groudina.ten.demo.dto.EndRoundEventDto;
import com.groudina.ten.demo.dto.NewRoundEventDto;
import com.groudina.ten.demo.exceptions.IllegalAnswerSubmissionException;
import com.groudina.ten.demo.exceptions.IllegalGameStateException;
import com.groudina.ten.demo.exceptions.RoundEndInNotStartedCompetitionException;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbRole;
import com.groudina.ten.demo.models.DbTeam;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnableEmbeddedMongo
@Log4j2
class GameManagementServiceImplTest {

    final static int roundsCountDefault = 2;

    @Autowired
    DbUserRepository userRepository;

    @Autowired
    DbRolesRepository rolesRepository;

    @Autowired
    DbCompetitionsRepository competitionsRepository;

    @Autowired
    DbTeamsRepository teamsRepository;

    @Autowired
    DbAnswersRepository answersRepository;

    @Autowired
    DbCompetitionMessagesRepository messagesRepository;

    @Autowired
    DbCompetitionProcessInfosRepository competitionProcessInfosRepository;

    @Autowired
    DbCompetitionRoundInfosRepository competitionRoundInfosRepository;

    @Autowired
    DbRoundResultElementsRepository roundResultElementsRepository;

    @Autowired
    IGameManagementService gameManagementService;

    @Configuration
    class MongoConfig {}

    @BeforeEach
    void setup() {
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
        competitionProcessInfosRepository.deleteAll().block();
        competitionRoundInfosRepository.deleteAll().block();
        roundResultElementsRepository.deleteAll().block();
        messagesRepository.deleteAll().block();
        gameManagementService.clear().block();
        gameManagementService.clear().block();
    }

    DbCompetition commonPart() {
        var competitionParams = DbCompetition.Parameters.builder()
                .maxTeamSize(3)
                .maxTeamsAmount(3)
                .roundsCount(roundsCountDefault)
                .expensesFormula(List.of("1", "2", "3"))
                .demandFormula(List.of("100", "10"))
                .roundLengthInSeconds(60)
                .teamLossUpperbound(10000)
                .build();
        var comp = DbCompetition.builder()
                .parameters(competitionParams)
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .build();

        comp = competitionsRepository.save(comp).block();

        var team1 = teamsRepository.save(DbTeam.builder().idInGame(0).sourceCompetition(comp).build()).block();
        var team2 = teamsRepository.save(DbTeam.builder().idInGame(1).sourceCompetition(comp).build()).block();

        comp.addTeam(team1);
        comp.addTeam(team2);

        comp = competitionsRepository.save(comp).block();

        return comp;
    }

    @Test
    void testTeamAnswersAndRoundEventsFlux() {
        var comp = commonPart();
        gameManagementService.startCompetition(comp).block();

        var teams = teamsRepository.findAll(Sort.by(Sort.Direction.ASC, "idInGame")).collectList().block();

        assertEquals(comp.getCompetitionProcessInfo().getCurrentRoundNumber(), 1);

        var answersVerifier = StepVerifier.create(gameManagementService.teamsAnswersEvents(comp));

        gameManagementService.submitAnswer(comp, teams.get(0), 20, 1).block();

        assertEquals(comp.getCompetitionProcessInfo().getCurrentRound().getAnswerList().size(), 1);

        gameManagementService.submitAnswer(comp, teams.get(1), 10, 1).block();

        answersVerifier.consumeNextWith((roundTeamAnswerDto) -> {
            assertEquals(roundTeamAnswerDto.getRoundNumber(), 1);
            assertEquals(roundTeamAnswerDto.getTeamAnswer(), 20);
            assertEquals(roundTeamAnswerDto.getTeamIdInGame(), 0);
        }).expectNextCount(1).thenCancel().verify();

        StepVerifier.create(gameManagementService.submitAnswer(comp, teams.get(1), 30, 0))
                .expectError(IllegalAnswerSubmissionException.class).verify();

        DbCompetition finalComp = comp;

        var tmp = StepVerifier.create(gameManagementService.beginEndRoundEvents(comp))
                .consumeNextWith((event) -> {
                    assertEquals(event.getType(), "NewRound");
                    NewRoundEventDto newRoundEventDto = (NewRoundEventDto) event;
                    assertEquals(newRoundEventDto.getRoundNumber(), 1);
                    assertEquals(newRoundEventDto.getRoundLength(), finalComp.getParameters().getRoundLengthInSeconds());
                }).consumeNextWith((event) -> {
                    assertEquals(event.getType(), "EndRound");
                }).thenCancel().verifyLater();

        gameManagementService.endCurrentRound(comp).block();

        tmp.verify();

        //check replay history size
        StepVerifier.create(gameManagementService.beginEndRoundEvents(comp)).consumeNextWith((event) -> {
            assertEquals(event.getType(), "EndRound");
        }).thenCancel().verify();

        gameManagementService.startNewRound(comp).block();

        tmp = StepVerifier.create(gameManagementService.beginEndRoundEvents(comp))
                .consumeNextWith((event) -> {
                    assertEquals(event.getType(), "NewRound");
                    NewRoundEventDto newRoundEventDto = (NewRoundEventDto)event;
                    assertEquals(newRoundEventDto.getRoundLength(), 60);
                }).consumeNextWith((event) -> {
                    assertEquals(event.getType(), "NewRound");
                    NewRoundEventDto newRoundEventDto = (NewRoundEventDto)event;
                    assertEquals(newRoundEventDto.getRoundLength(), 120);
                })
                .thenCancel().verifyLater();

        gameManagementService.addMinuteToCurrentRound(comp).block();

        tmp.verify();

        gameManagementService.endCurrentRound(comp).block();

        StepVerifier.create(gameManagementService.beginEndRoundEvents(comp)).consumeNextWith((event) -> {
            assertEquals(event.getType(), "EndRound");
            EndRoundEventDto endRoundEventDto = (EndRoundEventDto)event;

            assertTrue(endRoundEventDto.isEndOfGame());
        }).thenCancel().verify();
    }

    @Test
    void testMessages() {
        var competitionParams = DbCompetition.Parameters.builder()
                .maxTeamSize(3)
                .maxTeamsAmount(3)
                .roundsCount(2)
                .roundLengthInSeconds(60)
                .build();
        var comp = DbCompetition.builder()
                .parameters(competitionParams)
                .state(DbCompetition.State.Registration)
                .pin("1234")
                .build();

        comp = competitionsRepository.save(comp).block();

        gameManagementService.startCompetition(comp).block();

        var stepVerifier = StepVerifier.create(gameManagementService.getCompetitionMessages(comp))
                .consumeNextWith(competitionMessageDto -> {
                    assertEquals(competitionMessageDto.getMessage(), "1");
                    assertTrue(Math.abs(competitionMessageDto.getSendTime() - LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond()) <= 2);
                }).thenCancel().verifyLater();

        gameManagementService.addMessage(comp, CompetitionMessageRequest.builder().message("1").build()).block();

        stepVerifier.verify();

        gameManagementService.addMessage(comp, CompetitionMessageRequest.builder().message("2").build()).block();

        StepVerifier.create(gameManagementService.getCompetitionMessages(comp))
                .consumeNextWith(competitionMessageDto -> {
                    assertEquals(competitionMessageDto.getMessage(), "1");
                }).consumeNextWith(competitionMessageDto -> {
                    assertEquals(competitionMessageDto.getMessage(), "2");
                    assertTrue(Math.abs(competitionMessageDto.getSendTime() - LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond()) <= 2);
                }).thenCancel().verify();
    }

    @Test
    void testSubmitInNotStartedGame() {
        var comp = commonPart();

        StepVerifier.create(gameManagementService.submitAnswer(comp, null, 1, 1))
                .expectErrorSatisfies((ex) -> {
                    assertTrue(ex instanceof IllegalAnswerSubmissionException);
                    assertTrue(ex.getMessage().contains("not started"));
                }).verify();
    }

    @Test
    void testSubmitWrongRound() {
        var comp = commonPart();

        gameManagementService.startCompetition(comp).block();

        StepVerifier.create(gameManagementService.submitAnswer(comp, null, 1, -229))
                .expectErrorSatisfies((ex) -> {
                    assertTrue(ex instanceof IllegalAnswerSubmissionException);
                    assertTrue(ex.getMessage().contains("round"));
                }).verify();
    }

    @Test
    void submitTwice() {
        var comp = commonPart();

        gameManagementService.startCompetition(comp).block();

        var team = teamsRepository.findAll().collectList().block().get(0);
        StepVerifier.create(gameManagementService.submitAnswer(comp, team, 10, 1))
                .verifyComplete();

        StepVerifier.create(gameManagementService.submitAnswer(comp, team, 20, 1))
                .verifyComplete();

        var allAnswers = answersRepository.findAll().collectList().block();
        assertEquals(allAnswers.size(), 1);
        assertEquals(allAnswers.get(0).getValue(), 20);
    }

    @Test
    void testEndRoundInNotStartedGame() {
        var comp = commonPart();

        StepVerifier.create(gameManagementService.endCurrentRound(comp))
                .expectErrorSatisfies((ex) -> {
                    assertTrue(ex instanceof RoundEndInNotStartedCompetitionException);
                    assertTrue(ex.getMessage().contains("not started"));
                }).verify();
    }

    @Test
    void testStartRoundGameNotInProcess() {
        var comp = commonPart();

        StepVerifier.create(gameManagementService.startNewRound(comp))
                .expectError(IllegalGameStateException.class).verify();

        gameManagementService.startCompetition(comp).block();
        gameManagementService.endCurrentRound(comp).block();
        gameManagementService.startNewRound(comp).block();
        gameManagementService.endCurrentRound(comp).block();

        StepVerifier.create(gameManagementService.startNewRound(comp))
                .expectError(IllegalGameStateException.class).verify();
        assertEquals(comp.getState(), DbCompetition.State.Ended);
    }

    @Test
    void testResultsStream() {
        var comp = commonPart();

        var teams = teamsRepository.findAll().collectList().block();

        gameManagementService.startCompetition(comp).block();

        gameManagementService.submitAnswer(comp, teams.get(0), 10, 1).block();

        var verifier = StepVerifier.create(gameManagementService.getRoundResultsEvents(comp))
                .consumeNextWith(ans -> {
                    assertEquals(ans.getIncome(), -63, 0.001);
                    assertEquals(ans.getRoundNumber(), 1);
                    assertEquals(ans.getTeamIdInGame(), 0);
                })
                .expectNextCount(1)
                .expectNoEvent(Duration.ofSeconds(1))
                .thenCancel().verifyLater();

        gameManagementService.submitAnswer(comp, teams.get(1), 30, 1).block();
        gameManagementService.endCurrentRound(comp).block();

        verifier.verify();
    }

    @Test
    void testPricesEvents() {
        var comp = commonPart();

        var teams = teamsRepository.findAll().collectList().block();

        gameManagementService.startCompetition(comp).block();

        gameManagementService.submitAnswer(comp, teams.get(0), 10, 1).block();
        gameManagementService.submitAnswer(comp, teams.get(1), 30, 1).block();

        var verifier = StepVerifier.create(gameManagementService.getRoundPricesEvents(comp))
                .consumeNextWith(dto -> {
                    assertEquals(dto.getPrice(), 6, 0.01);
                    assertEquals(dto.getRoundNumber(), 1);
                }).expectNoEvent(Duration.ofSeconds(1)).thenCancel().verifyLater();

        gameManagementService.endCurrentRound(comp).block();

        verifier.verify();
    }

    @Test
    void testEarlyRoundsEventSubscription() {
        var comp = commonPart();

        var verifier = StepVerifier.create(gameManagementService.beginEndRoundEvents(comp))
                .consumeNextWith(dto -> {
                    assertEquals(dto.getType(), "NewRound");
                }).thenCancel().verifyLater();
        gameManagementService.startCompetition(comp).block();
        verifier.verify();
    }

    @Test
    void checkBanTeams() {
        var comp = commonPart();
        var team = comp.getTeams().get(0);

        gameManagementService.startCompetition(comp).block();
        // TODO after merging with branch about not starting game
        //gameManagementService.startNewRound(comp).block();

        var banVerifier = StepVerifier.create(gameManagementService.getBannedTeamEvents(comp))
                .consumeNextWith(dto -> {
                    assertEquals(dto.getTeamIdInGame(), team.getIdInGame());
                }).thenCancel().verifyLater();
        var messageVerifier = StepVerifier.create(gameManagementService.getCompetitionMessages(comp))
                .consumeNextWith(dto -> {
                    System.out.println(dto.getMessage());
                }).thenCancel().verifyLater();

        gameManagementService.submitAnswer(comp, team, 1100, 1).block();
        gameManagementService.endCurrentRound(comp).block();
        banVerifier.verify();
        messageVerifier.verify();
    }
}