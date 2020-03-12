package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;

import java.util.Optional;

public interface IStudentTeamFinder {
    Optional<DbTeam> findTeamForStudent(DbCompetition competition, String userEmail);
}
