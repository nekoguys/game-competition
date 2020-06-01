package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamCreationCheckerImplTest {

    private TeamCreationCheckerImpl checker = new TeamCreationCheckerImpl();

    @Test
    void checkCreation() {
        DbUser captain = DbUser.builder().id("1").build();
        DbUser ally = DbUser.builder().id("2").build();
        DbUser newOne = DbUser.builder().id("3").build();
        var team = DbTeam.builder().captain(captain).build();
        team.addPlayer(ally);

        var competition = DbCompetition.builder().teams(List.of(team)).state(DbCompetition.State.Registration).build();

        assertTrue(checker.checkCreation(competition, newOne));
        assertFalse(checker.checkCreation(competition, DbUser.builder().id("1").build()));
    }
}