package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbCompetitionRoundInfo;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;

class EndRoundSchedulerImplTest {
    EndRoundSchedulerImpl endRoundScheduler = new EndRoundSchedulerImpl();

    @Test
    void testSimple() {
        var params = DbCompetition.Parameters.builder().roundLengthInSeconds(2).build();
        var round = DbCompetitionRoundInfo.builder().additionalMinutes(0).startTime(LocalDateTime.now()).id("1").build();

        var competition = DbCompetition.builder().parameters(params).build();

        StepVerifier.withVirtualTime(() -> endRoundScheduler.submitRoundForScheduler(competition, round).then(Mono.just(1)))
                .thenAwait(Duration.ofSeconds(2))
                .expectNextCount(1).verifyComplete();

    }

    @Test
    void testRemove() {
        var params = DbCompetition.Parameters.builder().roundLengthInSeconds(2).build();
        var round = DbCompetitionRoundInfo.builder().additionalMinutes(0).startTime(LocalDateTime.now()).id("1").build();

        var competition = DbCompetition.builder().parameters(params).build();

        StepVerifier.withVirtualTime(() -> endRoundScheduler.submitRoundForScheduler(competition, round).then(Mono.just(1)))
                .then(() -> {
                    endRoundScheduler.removeRoundFromScheduler(competition, round);
                })
                .expectNoEvent(Duration.ofSeconds(3))
                .thenCancel().verify();
    }
}