package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbRoundResultElement;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DbRoundResultElementsRepository extends ReactiveMongoRepository<DbRoundResultElement, String> {
}
