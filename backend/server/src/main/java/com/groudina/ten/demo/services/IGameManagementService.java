package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.CompetitionMessageDto;
import com.groudina.ten.demo.dto.CompetitionMessageRequest;
import com.groudina.ten.demo.dto.ITypedEvent;
import com.groudina.ten.demo.dto.RoundTeamAnswerDto;
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

    Mono<Void> endCurrentRound(DbCompetition competition);

    Mono<Void> startNewRound(DbCompetition competition);

    Mono<Void> addMessage(DbCompetition competition, CompetitionMessageRequest request);

    Mono<Void> clear();
}
