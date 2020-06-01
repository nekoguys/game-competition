package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbUser;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface DbUserRepository extends ReactiveMongoRepository<DbUser, String> {
    Mono<Boolean> existsByEmail(String email);
    Mono<DbUser> findOneByEmail(String email);
}
