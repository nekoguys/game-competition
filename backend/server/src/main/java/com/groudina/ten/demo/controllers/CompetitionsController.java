package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.*;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.services.*;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;
import java.util.ArrayList;

@Log4j2
@RequestMapping(path="/api/competitions", produces = {MediaType.APPLICATION_JSON_VALUE})
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RestController
public class CompetitionsController {
    private DbCompetitionsRepository competitionsRepository;
    private DbUserRepository userRepository;
    private DbTeamsRepository teamsRepository;
    private IEntitiesMapper<NewCompetition, DbCompetition> competitionMapper;
    private ICompetitionPinGenerator pinGenerator;
    private IAddTeamToCompetitionService addTeamToCompetitionService;
    private ITeamConnectionNotifyService teamConnectionNotifyService;
    private ITeamJoinService teamJoinService;

    public CompetitionsController(@Autowired DbCompetitionsRepository repository,
                                  @Autowired DbUserRepository userRepository,
                                  @Autowired DbTeamsRepository teamsRepository,
                                  @Autowired IEntitiesMapper<NewCompetition, DbCompetition> mapper,
                                  @Autowired ICompetitionPinGenerator pinGenerator,
                                  @Autowired IAddTeamToCompetitionService addTeamToCompetitionService,
                                  @Autowired ITeamConnectionNotifyService teamConnectionNotifyService,
                                  @Autowired ITeamJoinService teamJoinService) {
        this.competitionsRepository = repository;
        this.userRepository = userRepository;
        this.teamsRepository = teamsRepository;
        this.competitionMapper = mapper;
        this.pinGenerator = pinGenerator;
        this.addTeamToCompetitionService = addTeamToCompetitionService;
        this.teamConnectionNotifyService = teamConnectionNotifyService;
        this.teamJoinService = teamJoinService;
    }

    @PostMapping(value = "/create")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> createCompetition(Mono<Principal> principalMono, @Valid @RequestBody NewCompetition competition) {
        return principalMono.map(principal -> {
            log.error(principal.getName());
            return principal.getName();
        }).flatMap(userEmail -> {
            return userRepository.findOneByEmail(userEmail);
        }).flatMap(dbUser -> {
            ArrayList<Pair<String, ?>> params = new ArrayList<Pair<String, ?>>();
            params.add(Pair.of("owner", dbUser));
            System.out.println(competition.getState());
            if (competition.getState().equals(DbCompetition.State.Registration.toString().toLowerCase()))
                params.add(Pair.of("pin", pinGenerator.generate()));
            var dbCompetition = competitionMapper.map(competition, params);
            return competitionsRepository.save(dbCompetition);
        }).map(newCompetition -> {
            return ResponseEntity.ok(ResponseMessage.of("Competition Created Successfully"));
        });
    }

    @PostMapping(value = "/create_team")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity<ResponseMessage>> joinTeam(@Valid @RequestBody NewTeam newTeam) {
        return this.addTeamToCompetitionService.addTeamToCompetition(newTeam).map(team -> {
            return ResponseEntity.ok(ResponseMessage.of("Team created successfully"));
        }).onErrorResume(ex ->
                Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.of(ex.getMessage()))));
    }

    @PostMapping(value = "/check_pin")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity<GamePinCheckResponse>> checkIfGameExists(@Valid @RequestBody GamePinCheckRequest pinCheck) {
        return competitionsRepository.findByPin(pinCheck.getPin()).map(comp -> {
            if (comp.getState() != DbCompetition.State.Registration) {
                return ResponseEntity.ok(GamePinCheckResponse.of(false));
            }
            return ResponseEntity.ok(GamePinCheckResponse.of(true));
        }).defaultIfEmpty(ResponseEntity.ok(GamePinCheckResponse.of(false)));
    }

    @RequestMapping(value = "/team_join_events/{pin}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public Flux<ServerSentEvent<?>> subscribeToTeamJoinEvents(@PathVariable String pin) {
        return teamConnectionNotifyService.getTeamEventForGame(pin).map(e -> ServerSentEvent.builder(e).build());
    }

    @PostMapping(value = "/join_team")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> joinTeam(Mono<Principal> principalMono, @Valid @RequestBody JoinTeamRequest joinTeamRequest) {
        var compMono = this.competitionsRepository.findByPin(joinTeamRequest.getCompetitionPin());

        var userMono = principalMono
                .map(Principal::getName)
                .flatMap(userEmail -> {
                    return userRepository.findOneByEmail(userEmail);
                });

        return Mono.zip(compMono, userMono).flatMap(tuple -> {
            var user = tuple.getT2();
            var competition = tuple.getT1();

            return teamJoinService.joinTeam(competition, joinTeamRequest, user);
        }).map(team -> {
            this.teamConnectionNotifyService.registerTeam(team);
            return (ResponseEntity)ResponseEntity
                    .ok(JoinTeamResponse.builder().currentTeamName(team.getName()).build());
        })
                .onErrorResume(ex ->
                        Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.of(ex.getMessage()))))
                .defaultIfEmpty(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.of("No competition with pin: " + joinTeamRequest.getCompetitionPin())));
    }
}
