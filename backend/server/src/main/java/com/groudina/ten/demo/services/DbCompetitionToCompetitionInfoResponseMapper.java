package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.CompetitionInfoResponse;
import com.groudina.ten.demo.models.DbCompetition;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DbCompetitionToCompetitionInfoResponseMapper implements IEntitiesMapper<DbCompetition, CompetitionInfoResponse> {
    @Override
    public CompetitionInfoResponse map(DbCompetition from, Iterable<Pair<String, ?>> additionalFields) {
        var params = from.getParameters();
        return CompetitionInfoResponse.builder()
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
                .isAutoRoundEnding(params.isAutoRoundEnding())
                .teamLossUpperbound(params.getTeamLossUpperbound())
                .lastUpdateTime(getLastUpdateTime(from))
                .pin(from.getPin())
                .state(from.getState())
                .build();
    }

    private LocalDateTime getLastUpdateTime(DbCompetition from) {
        if (from.getLastModifiedDate() != null)
            return from.getLastModifiedDate();

        if (from.getCompetitionProcessInfo() != null)
            return from.getCompetitionProcessInfo().getCurrentRound().getStartTime();

        return null;
    }
}
