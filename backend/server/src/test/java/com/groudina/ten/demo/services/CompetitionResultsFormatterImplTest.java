package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.*;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class CompetitionResultsFormatterImplTest {

    private CompetitionResultsFormatterImpl resultsFormatter = new CompetitionResultsFormatterImpl();

    @Test
    void getCompetitionResults() {
        DbUser captain1 = DbUser.builder().email("captain1").build();
        DbUser captain2 = DbUser.builder().email("captain2").build();
        DbUser teamMember1 = DbUser.builder().email("user1").build();
        DbUser teamMember2 = DbUser.builder().email("user2").build();


        DbTeam team1 = DbTeam.builder().idInGame(0).captain(captain1).allPlayers(List.of(teamMember1)).build();
        DbTeam team2 = DbTeam.builder().idInGame(1).captain(captain2).allPlayers(List.of(teamMember2)).build();
        DbCompetition competition = DbCompetition.builder().teams(List.of(team1, team2)).build();

        DbAnswer answer11 = DbAnswer.builder().value(10).submitter(team1).build();
        DbAnswer answer12 = DbAnswer.builder().value(20).submitter(team2).build();
        DbAnswer answer21 = DbAnswer.builder().value(30).submitter(team1).build();
        DbAnswer answer22 = DbAnswer.builder().value(40).submitter(team2).build();

        DbRoundResultElement result11 = DbRoundResultElement.builder().team(team1).income(200).build();
        DbRoundResultElement result12 = DbRoundResultElement.builder().team(team2).income(300).build();

        DbRoundResultElement result21 = DbRoundResultElement.builder().team(team1).income(400).build();
        DbRoundResultElement result22 = DbRoundResultElement.builder().team(team2).income(500).build();

        DbCompetitionRoundInfo roundInfo1 = DbCompetitionRoundInfo.builder()
                .answerList(List.of(answer11, answer12))
                .roundResult(List.of(result11, result12))
                .price(10)
                .build();

        DbCompetitionRoundInfo roundInfo2 = DbCompetitionRoundInfo.builder()
                .answerList(List.of(answer21, answer22))
                .roundResult(List.of(result21, result22))
                .price(20).build();

        DbCompetitionProcessInfo processInfo = DbCompetitionProcessInfo.builder().roundInfos(List.of(roundInfo1, roundInfo2)).build();

        competition.setCompetitionProcessInfo(processInfo);

        var result = resultsFormatter.getCompetitionResults(competition);

        assertEquals(result.getIncome().get(1).get(0), 200, 0.01);
        assertEquals(result.getIncome().get(1).get(1), 300, 0.01);
        assertEquals(result.getIncome().get(2).get(0), 400, 0.01);
        assertEquals(result.getIncome().get(2).get(1), 500, 0.01);

        assertEquals(result.getPrices().get(1), 10, 0.01);
        assertEquals(result.getPrices().get(2), 20, 0.01);

        assertEquals(result.getProduced().get(1).get(0), 10);
        assertEquals(result.getProduced().get(1).get(1), 20);
        assertEquals(result.getProduced().get(2).get(0), 30);
        assertEquals(result.getProduced().get(2).get(1), 40);

        assertIterableEquals(result.getTeamMembers().get(0).stream().sorted().collect(Collectors.toList()), List.of("captain1", "user1"));
        assertIterableEquals(result.getTeamMembers().get(1).stream().sorted().collect(Collectors.toList()), List.of("captain2", "user2"));

        assertIterableEquals(result.getTeamsOrderInDecreasingByTotalPrice(), List.of(1, 0));
    }
}