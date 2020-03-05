package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbAnswer;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbCompetitionRoundInfo;
import com.groudina.ten.demo.models.DbTeam;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoundResultsCalculatorImplTest {
    private RoundResultsCalculatorImpl roundResultsCalculator = new RoundResultsCalculatorImpl();

    @Test
    void testResultsCalculations() {
        var params = DbCompetition.Parameters.builder()
                .roundsCount(2)
                .maxTeamsAmount(2)
                .expensesFormula(List.of("1", "2", "3"))
                .demandFormula(List.of("100", "10"))
                .build();
        var competition = DbCompetition.builder()
                .parameters(params)
                .build();
        var team1 = DbTeam.builder().sourceCompetition(competition).idInGame(0).id("1").build();
        var team2 = DbTeam.builder().sourceCompetition(competition).idInGame(1).id("2").build();
        var team3 = DbTeam.builder().sourceCompetition(competition).idInGame(2).id("3").build();
        competition.addTeam(team1); competition.addTeam(team2); competition.addTeam(team3);
        DbCompetitionRoundInfo roundInfo = DbCompetitionRoundInfo.builder()
                .answerList(
                        List.of(
                                DbAnswer.builder().submitter(team1).value(10).build(),
                                DbAnswer.builder().submitter(team2).value(30).build()
                        ))
                .build();

        var results_ = roundResultsCalculator.calculateResults(roundInfo, competition);
        var results = results_.getResults();
        assertEquals(6, results_.getPrice(), 0.01);
        assertEquals(results.get(0).getTeam().getId(), team1.getId());

        assertEquals(results.get(0).getIncome(), 6 * 10 - 10*10 - 10*2 - 3);
        assertEquals(results.get(1).getTeam().getId(), team2.getId());
        assertEquals(results.get(1).getIncome(), 6 * 30 - 30*30 - 30*2 - 3);
        assertEquals(results.get(2).getIncome(), -3);
    }
}