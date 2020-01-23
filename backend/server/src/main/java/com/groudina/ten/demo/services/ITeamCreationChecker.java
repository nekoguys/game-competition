package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbUser;

public interface ITeamCreationChecker {
    boolean checkCreation(DbCompetition competition, DbUser user);
}
