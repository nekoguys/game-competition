package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbCompetitionRoundInfo;
import reactor.core.publisher.Mono;

public interface IEndRoundsScheduler {
    //void submitRoundForScheduler(DbCompetition sourceCompetition, DbCompetitionRoundInfo roundInfo);
    Mono<DbCompetitionRoundInfo> submitRoundForScheduler(DbCompetition sourceCompetition, DbCompetitionRoundInfo roundInfo, int roundLength);
    Mono<DbCompetitionRoundInfo> submitRoundForScheduler(DbCompetition sourceCompetition, DbCompetitionRoundInfo roundInfo);
    void removeRoundFromScheduler(DbCompetition sourceCompetition, DbCompetitionRoundInfo roundInfo);
}
