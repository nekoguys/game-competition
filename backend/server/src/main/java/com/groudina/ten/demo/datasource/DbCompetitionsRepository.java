package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbCompetition;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DbCompetitionsRepository extends ReactiveMongoRepository<DbCompetition, String> {
    Mono<DbCompetition> findByPin(String pin);
}
