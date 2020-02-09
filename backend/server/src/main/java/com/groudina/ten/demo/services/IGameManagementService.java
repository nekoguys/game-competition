package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.EndRoundEventDto;
import com.groudina.ten.demo.dto.NewRoundEventDto;
import com.groudina.ten.demo.dto.RoundTeamAnswerDto;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGameManagementService {
    Mono<Void> startCompetition(DbCompetition competition);

    Mono<Void> addMinuteToCurrentRound(DbCompetition competition);

    Mono<Void> submitAnswer(DbCompetition competition, DbTeam team, int answer);

    //Mono<RoundResultDto> endCurrentRound(DbCompetition competition);

    Flux<RoundTeamAnswerDto> teamsAnswersEvents(DbCompetition competition);//computable flux for teacher

    Flux<EndRoundEventDto> getEndRoundEvents(DbCompetition competition);

    Flux<NewRoundEventDto> getNewRoundEvents(DbCompetition competition);//if same round number is produced then round length was changed
}
