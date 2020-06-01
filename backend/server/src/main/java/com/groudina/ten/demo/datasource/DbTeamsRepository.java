package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface DbTeamsRepository extends ReactiveMongoRepository<DbTeam, String> {
    Flux<DbTeam> findDbTeamBySourceCompetition(DbCompetition competition);
    Flux<DbTeam> findDbTeamsByCaptainOrAllPlayers(DbUser captain, DbUser player);
}
