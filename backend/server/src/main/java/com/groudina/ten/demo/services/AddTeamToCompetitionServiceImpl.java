package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.NewTeam;
import com.groudina.ten.demo.exceptions.*;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;

@Component
public class AddTeamToCompetitionServiceImpl implements IAddTeamToCompetitionService {
    private DbUserRepository userRepository;

    private DbCompetitionsRepository competitionsRepository;

    private DbTeamsRepository teamsRepository;

    private ITeamCreationChecker creationChecker;

    private ITeamIdGenerator teamIdGenerator;

    private ITeamConnectionNotifyService connectionNotifyService;

    public AddTeamToCompetitionServiceImpl(@Autowired DbUserRepository userRepository,
                                           @Autowired DbCompetitionsRepository competitionsRepository,
                                           @Autowired DbTeamsRepository teamsRepository,
                                           @Autowired ITeamCreationChecker creationChecker,
                                           @Autowired ITeamIdGenerator teamIdGenerator,
                                           @Autowired ITeamConnectionNotifyService connectionNotifyService) {
        this.userRepository = userRepository;
        this.competitionsRepository = competitionsRepository;
        this.teamsRepository = teamsRepository;
        this.creationChecker = creationChecker;
        this.teamIdGenerator = teamIdGenerator;
        this.connectionNotifyService = connectionNotifyService;
    }

    @Override
    public Mono<DbTeam> addTeamToCompetition(NewTeam newTeam) {
        if (Objects.isNull(newTeam.getName()) || newTeam.getName().length() < 4) {
            return Mono.error(new InvalidTeamNameException("Team name is empty or too small"));
        }
        var zipped = Mono.zip(userRepository.findOneByEmail(newTeam.getCaptainEmail()),
                competitionsRepository.findByPin(newTeam.getCompetitionId()));
        return zipped.
                flatMap((captainAndCompTuple) -> {
                    var captain = captainAndCompTuple.getT1();
                    var competition = captainAndCompTuple.getT2();

                    int maxTeamsAmount = competition.getParameters().getMaxTeamsAmount();
                    if (competition.getTeams().size() >= maxTeamsAmount) {
                        return Mono.error(new TooMuchTeamsInCompetitionException("There are too much teams in competition" +
                                " max amount: " + maxTeamsAmount));
                    }

                    if (competition.getTeams().stream().anyMatch(team -> Objects.equals(team.getName(), newTeam.getName()))) {
                        return Mono.error(new RepeatingTeamNameException("There is team with same name already"));
                    }

                    if (competition.getState() != DbCompetition.State.Registration) {
                        return Mono.error(new IllegalGameStateException("Illegal game state. This game is in state " +
                                competition.getState().name() + " but not in registration state"));
                    }

                    if (!creationChecker.checkCreation(competition, captain)) {
                        return Mono.error(new CaptainAlreadyCreatedGameException(newTeam.getCaptainEmail() +
                                " is Captain and is in another team already"));
                    }

                    var dbTeam = DbTeam.builder()
                            .password(newTeam.getPassword())
                            .captain(captain)
                            .sourceCompetition(competition)
                            .idInGame(teamIdGenerator.generate(competition))
                            .name(newTeam.getName())
                            .build();
                    competition.addTeam(dbTeam);
                    return teamsRepository.save(dbTeam).flatMap(team -> {
                        this.connectionNotifyService.registerTeam(team);
                        return competitionsRepository.save(competition);
                    }).then(Mono.just(dbTeam)).switchIfEmpty(Mono.error(new WrongCompetitionParametersException("Game or captain not found")));
        });
    }
}
