package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.CompetitionCloneInfoResponse;
import com.groudina.ten.demo.models.DbCompetition;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

@Component
public class DbCompetitionToCompetitionCloneInfoResponseMapper implements IEntitiesMapper<DbCompetition, CompetitionCloneInfoResponse> {
    @Override
    public CompetitionCloneInfoResponse map(DbCompetition from, Iterable<Pair<String, ?>> additionalFields) {
        var params = from.getParameters();
        return CompetitionCloneInfoResponse.builder()
                .name(params.getName())
                .demandFormula(String.join(";", params.getDemandFormula()))
                .expensesFormula(String.join(";", params.getExpensesFormula()))
                .instruction(params.getInstruction())
                .maxTeamsAmount(params.getMaxTeamsAmount())
                .maxTeamSize(params.getMaxTeamSize())
                .roundLength(params.getRoundLengthInSeconds())
                .roundsCount(params.getRoundsCount())
                .shouldEndRoundBeforeAllAnswered(params.isShouldEndRoundBeforeAllAnswered())
                .shouldShowResultTableInEnd(params.isShouldShowResultTableInEnd())
                .shouldShowStudentPreviousRoundResults(params.isShouldShowStudentPreviousRoundResults())
                .build();
    }
}
