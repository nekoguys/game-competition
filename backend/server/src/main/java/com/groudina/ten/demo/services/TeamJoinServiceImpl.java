package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.dto.JoinTeamRequest;
import com.groudina.ten.demo.exceptions.IllegalGameStateException;
import com.groudina.ten.demo.exceptions.NoSuchTeamNameInCompetitionException;
import com.groudina.ten.demo.exceptions.UserTriedToJoinManyTeamsException;
import com.groudina.ten.demo.exceptions.WrongTeamJoinPasswordException;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Service
public class TeamJoinServiceImpl implements ITeamJoinService {

    private DbTeamsRepository teamsRepository;

    public TeamJoinServiceImpl(@Autowired DbTeamsRepository repository) {
        this.teamsRepository = repository;
    }

    @Override
    public Mono<DbTeam> joinTeam(DbCompetition competition, JoinTeamRequest request, DbUser newTeamMember) {

        if (!competition.getState().equals(DbCompetition.State.Registration)) {
            return Mono.error(new IllegalGameStateException());
        }

        var optTeam = competition.getTeams().stream().filter(_team -> _team.getName().equals(request.getTeamName())).findFirst();

        if (optTeam.isPresent()) {
            var team = optTeam.get();
            if (team.getSourceCompetition()
                    .getTeams()
                    .stream()
                    .anyMatch(_team -> {
                        var allPlayers = _team.getAllPlayers();
                        return allPlayers.contains(newTeamMember);
            })) {
                return Mono.error(new UserTriedToJoinManyTeamsException());
            }

            if (team.getPassword().equals(request.getPassword())) {
                team.addPlayer(newTeamMember);
                return teamsRepository.save(team);
            } else {
                return Mono.error(new WrongTeamJoinPasswordException());
            }
        } else {
            return Mono.error(new NoSuchTeamNameInCompetitionException());
        }
    }
}
