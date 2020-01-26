package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DbTeamsRepository extends ReactiveMongoRepository<DbTeam, String> {
    Flux<DbTeam> findDbTeamBySourceCompetition(DbCompetition competition);

    Mono<DbTeam> findByName(String name);
}
