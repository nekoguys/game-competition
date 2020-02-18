package com.groudina.ten.demo.services;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.*;
import com.groudina.ten.demo.dto.CompetitionMessageRequest;
import com.groudina.ten.demo.dto.EndRoundEventDto;
import com.groudina.ten.demo.dto.NewRoundEventDto;
import com.groudina.ten.demo.exceptions.IllegalAnswerSubmissionException;
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
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@EnableEmbeddedMongo
@Log4j2
class GameManagementServiceImplTest {

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
    }

    @Test
    void testTeamAnswersAndRoundEventsFlux() {
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

        var team1 = teamsRepository.save(DbTeam.builder().idInGame(0).sourceCompetition(comp).build()).block();
        var team2 = teamsRepository.save(DbTeam.builder().idInGame(1).sourceCompetition(comp).build()).block();

        comp.addTeam(team1);
        comp.addTeam(team2);

        comp = competitionsRepository.save(comp).block();

        gameManagementService.startCompetition(comp).block();
        //comp = competitionsRepository.findAll().blockFirst();
        assertEquals(comp.getCompetitionProcessInfo().getCurrentRoundNumber(), 1);

        var answersVerifier = StepVerifier.create(gameManagementService.teamsAnswersEvents(comp));

        gameManagementService.submitAnswer(comp, team1, 20, 1).block();

        assertEquals(comp.getCompetitionProcessInfo().getCurrentRound().getAnswerList().size(), 1);

        gameManagementService.submitAnswer(comp, team2, 10, 1).block();

        answersVerifier.consumeNextWith((roundTeamAnswerDto) -> {
            assertEquals(roundTeamAnswerDto.getRoundNumber(), 1);
            assertEquals(roundTeamAnswerDto.getTeamAnswer(), 20);
            assertEquals(roundTeamAnswerDto.getTeamIdInGame(), 0);
        }).expectNextCount(1).thenCancel().verify();

        StepVerifier.create(gameManagementService.submitAnswer(comp, team2, 30, 0))
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
}