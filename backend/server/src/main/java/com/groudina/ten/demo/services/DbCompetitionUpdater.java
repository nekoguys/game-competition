package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.dto.NewCompetition;
import com.groudina.ten.demo.models.DbCompetition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class DbCompetitionUpdater implements IEntityUpdater<DbCompetition, NewCompetition> {

    private DbCompetitionsRepository repository;

    public DbCompetitionUpdater(@Autowired DbCompetitionsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<DbCompetition> update(DbCompetition entity, NewCompetition competition) {
        if (!Objects.isNull(competition.getState())) {
            var state = DbCompetition.State.valueOf(competition.getState());
            entity.setState(state);
        }
        if (!Objects.isNull(competition.getMaxTeamsAmount())) {
            entity.getParameters().setMaxTeamsAmount(competition.getMaxTeamsAmount());
        }

        if (!Objects.isNull(competition.getMaxTeamSize())) {
            entity.getParameters().setMaxTeamSize(competition.getMaxTeamSize());
        }

        if (!Objects.isNull(competition.getName())) {
            entity.getParameters().setName(competition.getName());
        }

        if (!Objects.isNull(competition.getRoundLength())) {
            entity.getParameters().setRoundLengthInSeconds(competition.getRoundLength());
        }

        if (!Objects.isNull(competition.getDemandFormula())) {
            entity.getParameters().setDemandFormula(competition.getDemandFormula());
        }

        if (!Objects.isNull(competition.getExpensesFormula())) {
            entity.getParameters().setExpensesFormula(competition.getExpensesFormula());
        }

        if (!Objects.isNull(competition.getInstruction())) {
            entity.getParameters().setInstruction(competition.getInstruction());
        }

        if (!Objects.isNull(competition.getRoundsCount())) {
            entity.getParameters().setRoundsCount(competition.getRoundsCount());
        }

        if (!Objects.isNull(competition.getShouldEndRoundBeforeAllAnswered())) {
            entity.getParameters().setShouldEndRoundBeforeAllAnswered(competition.getShouldEndRoundBeforeAllAnswered());
        }

        if (!Objects.isNull(competition.getShouldShowResultTableInEnd())) {
            entity.getParameters().setShouldShowResultTableInEnd(competition.getShouldShowResultTableInEnd());
        }

        if (!Objects.isNull(competition.getShouldShowStudentPreviousRoundResults())) {
            entity.getParameters().setShouldShowStudentPreviousRoundResults(competition.getShouldShowStudentPreviousRoundResults());
        }

        return repository.save(entity);
    }
}
