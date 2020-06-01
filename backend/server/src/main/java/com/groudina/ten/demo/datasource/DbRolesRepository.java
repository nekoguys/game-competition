package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbRole;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface DbRolesRepository extends ReactiveMongoRepository<DbRole, String> {
    Mono<DbRole> findByName(String name);
}
