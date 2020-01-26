package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.JoinTeamRequest;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import reactor.core.publisher.Mono;

public interface ITeamJoinService {
    Mono<DbTeam> joinTeam(DbCompetition competition, JoinTeamRequest joinTeamRequest, DbUser newTeamMember);
}
