package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbUser;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DbCompetitionsRepository extends ReactiveMongoRepository<DbCompetition, String> {
    Mono<DbCompetition> findByPin(String pin);

    Flux<DbCompetition> findAllByOwner(DbUser owner);

    Flux<DbCompetition> findAllByParameters_IsAutoRoundEndingTrueAndState(DbCompetition.State state);
}
