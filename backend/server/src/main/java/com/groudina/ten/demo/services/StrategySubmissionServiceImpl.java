package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.exceptions.IllegalStrategySubmissionException;
import com.groudina.ten.demo.models.DbCompetition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class StrategySubmissionServiceImpl implements IStrategySubmissionService {
    public static final int MINUTES_TO_WAIT = 10;
    private IStudentTeamFinder studentTeamFinder;
    private DbTeamsRepository teamsRepository;

    public StrategySubmissionServiceImpl(
            @Autowired IStudentTeamFinder studentTeamFinder,
            @Autowired DbTeamsRepository teamsRepository
    ) {
        this.studentTeamFinder = studentTeamFinder;
        this.teamsRepository = teamsRepository;
    }

    @Override
    public Mono<Void> submitStrategy(String submitterEmail, DbCompetition competition, IStrategySubmissionService.StrategyHolder holder) {
        if (competition.getState() != DbCompetition.State.InProcess && competition.getState() != DbCompetition.State.Ended) {
            return Mono.error(new IllegalStrategySubmissionException("Can't submit strategy, wrong game state: not in process and not ended"));
        }

        if (competition.getState() == DbCompetition.State.Ended) {
            if (competition.getCompetitionProcessInfo().getCurrentRound().getEndTime().plusMinutes(MINUTES_TO_WAIT)
                    .isBefore(LocalDateTime.now())) {
                return Mono.error(new IllegalStrategySubmissionException("Can't submit strategy, it's too late"));
            }
        }

        var teamOpt = studentTeamFinder.findTeamForStudent(competition, submitterEmail);
        if (teamOpt.isEmpty()) {
            return Mono.error(new IllegalStrategySubmissionException("User with email: " + submitterEmail + " has no team"));
        }
        var team = teamOpt.get();
        if (team.getCaptain().getEmail().equals(submitterEmail)) {
            team.setStrategy(holder.getStrategy());
            return this.teamsRepository.save(team).then();
        } else {
            return Mono.error(new IllegalStrategySubmissionException("User with email: " + submitterEmail + " is not team \"" + team.getName() + "\" captain"));
        }
    }
}
