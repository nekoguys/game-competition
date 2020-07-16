package com.groudina.ten.demo.services;

import com.groudina.ten.demo.EnableEmbeddedMongo;
import com.groudina.ten.demo.datasource.*;
import com.groudina.ten.demo.models.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;


@SpringBootTest
@EnableEmbeddedMongo
class StrategySubmissionServiceImplTest {
    @Autowired
    DbUserRepository userRepository;

    @Autowired
    DbRolesRepository rolesRepository;

    @Autowired
    DbCompetitionsRepository competitionsRepository;

    @Autowired
    DbTeamsRepository teamsRepository;

    @Autowired
    DbCompetitionProcessInfosRepository competitionProcessInfosRepository;

    @Autowired
    DbCompetitionRoundInfosRepository competitionRoundInfosRepository;

    @Autowired
    StrategySubmissionServiceImpl strategySubmissionService;

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
        userRepository.deleteAll().block();
        rolesRepository.deleteAll().block();
        competitionsRepository.deleteAll().block();
        teamsRepository.deleteAll().block();
        competitionProcessInfosRepository.deleteAll().block();
        competitionRoundInfosRepository.deleteAll().block();
    }

    @Test
    void strategySubmitterNotInTeam() {
        var captain = userRepository.save(DbUser.builder().email("user1").build()).block();
        var competition = this.competitionsRepository.save(
                DbCompetition.builder()
                        .pin("1")
                        .state(DbCompetition.State.InProcess)
                        .teams(List.of(teamsRepository.save(DbTeam.builder().captain(captain).build()).block()))
                        .build()
        ).block();
        var user2 = userRepository.save(DbUser.builder().email("user2").build()).block();

        StepVerifier.create(
                this.strategySubmissionService.submitStrategy(user2.getEmail(), competition, new IStrategySubmissionService.StrategyHolder("strat"))
        ).expectErrorSatisfies(el -> {
            assertTrue(el.getMessage().toLowerCase().contains("has no team"));
        }).verify();
    }

    @Test
    void strategySubmissionNotCaptain() {
        var captain = userRepository.save(DbUser.builder().email("user1").build()).block();
        var user2 = userRepository.save(DbUser.builder().email("user2").build()).block();

        var competition = this.competitionsRepository.save(
                DbCompetition.builder()
                        .pin("1")
                        .state(DbCompetition.State.InProcess)
                        .teams(List.of(teamsRepository.save(DbTeam.builder().captain(captain).allPlayers(List.of(user2)).build()).block()))
                        .build()
        ).block();

        StepVerifier.create(
                this.strategySubmissionService.submitStrategy(user2.getEmail(), competition, new IStrategySubmissionService.StrategyHolder("strat"))
        ).expectErrorSatisfies(el -> {
            assertTrue(el.getMessage().toLowerCase().matches("(.*)is not team (.*) captain(.*)"));
        }).verify();
    }

    @Test
    void strategySubmissionCaptain() {
        var captain = userRepository.save(DbUser.builder().email("user1").build()).block();
        var user2 = userRepository.save(DbUser.builder().email("user2").build()).block();

        var competition = this.competitionsRepository.save(
                DbCompetition.builder()
                        .pin("1")
                        .state(DbCompetition.State.InProcess)
                        .teams(List.of(teamsRepository.save(DbTeam.builder().captain(captain).allPlayers(List.of(user2)).build()).block()))
                        .build()
        ).block();

        StepVerifier.create(
                this.strategySubmissionService.submitStrategy(captain.getEmail(), competition, new IStrategySubmissionService.StrategyHolder("strat"))
        ).expectComplete().verify();
    }

    @Test
    void testEndedCompetitionNoTimeout() {
        var captain = userRepository.save(DbUser.builder().email("user1").build()).block();
        var user2 = userRepository.save(DbUser.builder().email("user2").build()).block();

        var competition = this.competitionsRepository.save(
                DbCompetition.builder()
                        .pin("1")
                        .state(DbCompetition.State.Ended)
                        .teams(List.of(teamsRepository.save(DbTeam.builder().captain(captain).allPlayers(List.of(user2)).build()).block()))
                        .competitionProcessInfo(competitionProcessInfosRepository.save(
                                DbCompetitionProcessInfo.builder().currentRoundNumber(1).roundInfos(
                                        List.of(
                                                competitionRoundInfosRepository.save(
                                                    DbCompetitionRoundInfo.builder().endTime(LocalDateTime.now().minusMinutes(StrategySubmissionServiceImpl.MINUTES_TO_WAIT - 1)).build()
                                                ).block()
                                        )
                                ).build()
                        ).block())
                        .build()
        ).block();

        StepVerifier.create(
                this.strategySubmissionService.submitStrategy(captain.getEmail(), competition, new IStrategySubmissionService.StrategyHolder("strat"))
        ).expectComplete().verify();
    }

    @Test
    void testEndedCompetitionWithTimeout() {
        var captain = userRepository.save(DbUser.builder().email("user1").build()).block();
        var user2 = userRepository.save(DbUser.builder().email("user2").build()).block();

        var competition = this.competitionsRepository.save(
                DbCompetition.builder()
                        .pin("1")
                        .state(DbCompetition.State.Ended)
                        .teams(List.of(teamsRepository.save(DbTeam.builder().captain(captain).allPlayers(List.of(user2)).build()).block()))
                        .competitionProcessInfo(competitionProcessInfosRepository.save(
                                DbCompetitionProcessInfo.builder().currentRoundNumber(1).roundInfos(
                                        List.of(
                                                competitionRoundInfosRepository.save(
                                                        DbCompetitionRoundInfo.builder().endTime(LocalDateTime.now().minusMinutes(StrategySubmissionServiceImpl.MINUTES_TO_WAIT + 1)).build()
                                                ).block()
                                        )
                                ).build()
                        ).block())
                        .build()
        ).block();

        StepVerifier.create(
                this.strategySubmissionService.submitStrategy(captain.getEmail(), competition, new IStrategySubmissionService.StrategyHolder("strat"))
        ).expectErrorSatisfies(el -> {
            assertTrue(el.getMessage().toLowerCase().matches("(.*)too late(.*)"));
        }).verify();
    }
}