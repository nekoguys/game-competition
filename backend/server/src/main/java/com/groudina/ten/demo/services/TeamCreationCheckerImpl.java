package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbUser;
import org.springframework.stereotype.Component;

@Component
public class TeamCreationCheckerImpl implements ITeamCreationChecker {
    @Override
    public boolean checkCreation(DbCompetition competition, DbUser user) {
        return competition.getTeams().stream().noneMatch(team -> team.getAllPlayers().contains(user));
    }
}
