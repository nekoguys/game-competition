package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.NewCompetition;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.util.Pair;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class NewCompetitionToDbMapperTest {

    private NewCompetitionToDbMapper mapper;
    private NewCompetition competitionDto;

    @BeforeEach
    public void setUp() {
        mapper = new NewCompetitionToDbMapper();
        competitionDto = NewCompetition.builder()
                .state("Draft")
                .demandFormula(List.of("1", "2"))
                .expensesFormula(List.of("1", "2", "3"))
                .instruction("instr")
                .maxTeamsAmount(2)
                .maxTeamSize(5)
                .name("name")
                .roundLength(6)
                .roundsCount(7)
                .shouldEndRoundBeforeAllAnswered(false)
                .shouldShowResultTableInEnd(true)
                .shouldShowStudentPreviousRoundResults(false).build();
    }

    private void defaultAsserts(NewCompetition competitionDto, DbCompetition result) {
        assertEquals(competitionDto.getDemandFormula(), result.getParameters().getDemandFormula());
        assertEquals(competitionDto.getExpensesFormula(), result.getParameters().getExpensesFormula());
        assertEquals(competitionDto.getInstruction(), result.getParameters().getInstruction());
        assertEquals(competitionDto.getMaxTeamsAmount(), result.getParameters().getMaxTeamsAmount());
        assertEquals(competitionDto.getMaxTeamSize(), result.getParameters().getMaxTeamSize());
        assertEquals(competitionDto.getName(), result.getParameters().getName());
        assertEquals(competitionDto.getRoundLength(), result.getParameters().getRoundLengthInSeconds());
        assertEquals(competitionDto.getRoundsCount(), result.getParameters().getRoundsCount());
        assertEquals(competitionDto.getState(), result.getState().name());
        assertEquals(competitionDto.getShouldEndRoundBeforeAllAnswered(), result.getParameters().isShouldEndRoundBeforeAllAnswered());
        assertEquals(competitionDto.getShouldShowResultTableInEnd(), result.getParameters().isShouldShowResultTableInEnd());
        assertEquals(competitionDto.getShouldShowStudentPreviousRoundResults(), result.getParameters().isShouldShowStudentPreviousRoundResults());

    }

    @Test
    void mapWithoutAdditionalParams() {
        var result = mapper.map(competitionDto, null);
        defaultAsserts(competitionDto, result);
    }

    @Test
    void mapWithOwner() {
        var owner = DbUser.builder().id("id").email("email").password("pass").build();
        var result = mapper.map(competitionDto, List.of(Pair.of("owner", owner)));
        defaultAsserts(competitionDto, result);
        assertEquals(owner, result.getOwner());
    }
}