package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbUser;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface DbUserRepository extends ReactiveMongoRepository<DbUser, String> {
    Mono<Boolean> existsByEmail(String email);
    Mono<DbUser> findOneByEmail(String email);

    @Query(value = "{ $or: [" +
            "{ email: { $regex: ?0, $options: 'i' } }," +
            "{ 'profile.surname': { $regex: ?0, $options: 'i' } }," +
            "{ 'profile.name': { $regex: ?0, $options: 'i' } }" +
            "]}")
    Flux<DbUser> findByRegex(String pattern, Pageable page);
}
