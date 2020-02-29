package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbCompetitionMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DbCompetitionMessagesRepository extends ReactiveMongoRepository<DbCompetitionMessage, String> {
}
