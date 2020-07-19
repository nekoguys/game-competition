package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.*;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGameManagementService {
    Mono<Void> startCompetition(DbCompetition competition);

    Mono<Void> addMinuteToCurrentRound(DbCompetition competition);

    Mono<Void> submitAnswer(DbCompetition competition, DbTeam team, int answer, int roundNumber);

    Flux<RoundTeamAnswerDto> teamsAnswersEvents(DbCompetition competition);//computable flux for teacher

    Flux<ITypedEvent> beginEndRoundEvents(DbCompetition competition);

    Flux<CompetitionMessageDto> getCompetitionMessages(DbCompetition competition);

    Flux<RoundTeamResultDto> getRoundResultsEvents(DbCompetition competition);

    Flux<PriceInRoundDto> getRoundPricesEvents(DbCompetition competition);

    Flux<TeamBanEventDto> getBannedTeamEvents(DbCompetition competition);

    Mono<Void> endCurrentRound(DbCompetition competition);

    Mono<Void> startNewRound(DbCompetition competition);

    Mono<Void> addMessage(DbCompetition competition, CompetitionMessageRequest request);

    Mono<Void> changeRoundLength(DbCompetition competition, int newRoundLength);

    Mono<DbCompetition> restartGame(DbCompetition competition);

    Mono<Void> clear();
}
