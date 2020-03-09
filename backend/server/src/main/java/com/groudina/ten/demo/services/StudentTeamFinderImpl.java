package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.Optional;

@Component
public class StudentTeamFinderImpl implements IStudentTeamFinder {
    @Override
    public Optional<DbTeam> findTeamForStudent(@NotNull DbCompetition competition, @NotNull String userEmail) {
        return competition.getTeams().stream()
                .filter(team -> team.getAllPlayers().stream().anyMatch(el -> el.getEmail().equals(userEmail)))
                .findAny();
    }
}
