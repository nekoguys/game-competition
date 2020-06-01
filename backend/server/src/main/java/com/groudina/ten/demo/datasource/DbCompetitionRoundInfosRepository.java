package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbCompetitionRoundInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DbCompetitionRoundInfosRepository extends ReactiveMongoRepository<DbCompetitionRoundInfo, String> {
}
