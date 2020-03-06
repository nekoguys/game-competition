package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbUser;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.data.mongodb.repository.Tailable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface DbCompetitionsRepository extends ReactiveMongoRepository<DbCompetition, String> {
    Mono<DbCompetition> findByPin(String pin);

    Flux<DbCompetition> findAllByOwner(DbUser owner);
}
