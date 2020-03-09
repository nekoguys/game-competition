package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbVerificationToken;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface DbVerificationTokenRepository extends ReactiveMongoRepository<DbVerificationToken, String> {
    Mono<DbVerificationToken> findByToken(String token);
}
