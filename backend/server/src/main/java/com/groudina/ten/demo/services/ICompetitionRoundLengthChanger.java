package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import reactor.core.publisher.Mono;

public interface ICompetitionRoundLengthChanger {
    public Mono<DbCompetition> changeRoundLength(DbCompetition competition,int newRoundLength);
}
