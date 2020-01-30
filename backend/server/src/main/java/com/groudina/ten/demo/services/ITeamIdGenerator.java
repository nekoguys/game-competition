package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;

public interface ITeamIdGenerator {
    int generate(DbCompetition competition);
}
