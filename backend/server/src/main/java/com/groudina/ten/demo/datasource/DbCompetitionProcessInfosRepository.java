package com.groudina.ten.demo.datasource;

import com.groudina.ten.demo.models.DbCompetitionProcessInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface DbCompetitionProcessInfosRepository extends ReactiveMongoRepository<DbCompetitionProcessInfo, String> {
}
