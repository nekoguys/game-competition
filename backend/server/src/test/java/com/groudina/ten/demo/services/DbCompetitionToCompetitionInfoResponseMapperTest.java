package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DbCompetitionToCompetitionInfoResponseMapperTest {

    private DbCompetitionToCompetitionInfoResponseMapper mapper = new DbCompetitionToCompetitionInfoResponseMapper();

    @Test
    void map() {
        var parameters = DbCompetition.Parameters.builder()
                .maxTeamsAmount(1)
                .maxTeamSize(2)
                .name("name")
                .demandFormula(List.of("1", "2"))
                .expensesFormula(List.of("1", "2", "3"))
                .instruction("instr")
                .roundLengthInSeconds(60)
                .roundsCount(3)
                .shouldEndRoundBeforeAllAnswered(true)
                .shouldShowResultTableInEnd(false)
                .shouldShowStudentPreviousRoundResults(true)
                .build();
        var comp = DbCompetition.builder()
                .parameters(parameters)
                .state(DbCompetition.State.Draft)
                .pin("69")
                .build();

        var res = mapper.map(comp, null);

        assertEquals(parameters.getMaxTeamsAmount(), res.getMaxTeamsAmount());
        assertEquals(parameters.getMaxTeamSize(), res.getMaxTeamSize());
        assertEquals("1;2", res.getDemandFormula());
        assertEquals("1;2;3", res.getExpensesFormula());
        assertEquals(parameters.getName(), res.getName());
        assertEquals(parameters.getInstruction(), res.getInstruction());
        assertEquals(parameters.getRoundLengthInSeconds(), res.getRoundLength());
        assertEquals(parameters.getRoundsCount(), res.getRoundsCount());
        assertEquals(parameters.isShouldEndRoundBeforeAllAnswered(), res.isShouldEndRoundBeforeAllAnswered());
        assertEquals(parameters.isShouldShowResultTableInEnd(), res.isShouldShowResultTableInEnd());
        assertEquals(parameters.isShouldShowStudentPreviousRoundResults(), res.isShouldShowStudentPreviousRoundResults());
        assertEquals(comp.getPin(), res.getPin());
        assertEquals(comp.getState(), res.getState());
    }
}