package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TeamIdGeneratorImplTest {

    @Test
    void generate() {
        TeamIdGeneratorImpl idGenerator = new TeamIdGeneratorImpl();
        DbUser captain = DbUser.builder().id("1").build();
        DbUser ally = DbUser.builder().id("2").build();
        DbUser newOne = DbUser.builder().id("3").build();

        var competition = DbCompetition.builder()
                .state(DbCompetition.State.Registration)
                .pin("1234567890")
                .teams(new LinkedList<>())
                .build();

        var team1 = DbTeam.builder().captain(captain).idInGame(idGenerator.generate(competition)).build();
        var team2 = DbTeam.builder().captain(ally).idInGame(idGenerator.generate(competition)).build();
        var team3 = DbTeam.builder().captain(newOne).idInGame(idGenerator.generate(competition)).build();

        competition.addTeam(team1);
        competition.addTeam(team2);
        competition.addTeam(team3);

        List<DbTeam> teams = competition.getTeams();

        for (int i = 0; i < teams.size(); ++i)
            for (int j = i + 1; j < teams.size(); ++j)
                assertNotEquals(teams.get(i).getIdInGame(), teams.get(j).getIdInGame());
    }
}