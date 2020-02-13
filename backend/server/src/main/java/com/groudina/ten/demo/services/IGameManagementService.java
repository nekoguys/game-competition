package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.ITypedEvent;
import com.groudina.ten.demo.dto.RoundTeamAnswerDto;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGameManagementService {
    Mono<Void> startCompetition(DbCompetition competition);

    Mono<Void> addMinuteToCurrentRound(DbCompetition competition);

    Mono<Void> submitAnswer(DbCompetition competition, DbTeam team, int answer);

    Flux<RoundTeamAnswerDto> teamsAnswersEvents(DbCompetition competition);//computable flux for teacher

    Flux<ITypedEvent> beginEndRoundEvents(DbCompetition competition);

    Mono<Void> endCurrentRound(DbCompetition competition);

    Mono<Void> startNewRound(DbCompetition competition);
}
