package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.NewTeam;
import com.groudina.ten.demo.models.DbTeam;
import reactor.core.publisher.Mono;

public interface IAddTeamToCompetitionService {
    Mono<DbTeam> addTeamToCompetition(NewTeam newTeam);
}
