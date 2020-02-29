package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbAnswer;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DbAnswersRepository extends ReactiveMongoRepository<DbAnswer, String> {
}
