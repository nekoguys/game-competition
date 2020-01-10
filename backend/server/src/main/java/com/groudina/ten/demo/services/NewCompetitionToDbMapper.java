package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.NewCompetition;
import com.groudina.ten.demo.models.DbCompetition;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.util.Pair;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;

@Component
@Log4j2
public class NewCompetitionToDbMapper implements IEntitiesMapper<NewCompetition, DbCompetition> {

    private String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public DbCompetition map(NewCompetition from, @Nullable Iterable<Pair<String, ?>> additionalFields) {
        var builder = DbCompetition.builder();
        builder.state(DbCompetition.State.valueOf(capitalize(from.getState())))
                .parameters(DbCompetition.Parameters.builder()
                        .expensesFormula(from.getExpensesFormula())
                        .demandFormula(from.getDemandFormula())
                        .instruction(from.getInstruction())
                        .maxTeamsAmount(from.getMaxTeamsAmount())
                        .maxTeamSize(from.getMaxTeamSize())
                        .name(from.getName())
                        .roundLengthInSeconds(from.getRoundLength())
                        .roundsCount(from.getRoundsCount())
                        .shouldEndRoundBeforeAllAnswered(from.isShouldEndRoundBeforeAllAnswered())
                        .shouldShowResultTableInEnd(from.isShouldShowResultTableInEnd())
                        .shouldShowStudentPreviousRoundResults(from.isShouldShowStudentPreviousRoundResults())
                        .build());
        if (additionalFields != null) {
            for (var additionalField : additionalFields) {
                try {
                    builder.getClass().getMethod(additionalField.getFirst(), additionalField.getSecond().getClass()).invoke(builder, additionalField.getSecond());
                } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    log.error("Cant convert NewCompetition to DbCompetition " + this.getClass().getName() + " class.");
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            }
        }

        return builder.build();
    }
}
