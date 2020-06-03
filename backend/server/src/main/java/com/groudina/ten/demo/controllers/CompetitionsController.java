package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.datasource.DbUserRepository;
import com.groudina.ten.demo.dto.*;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Objects;

@RequestMapping(path="/api/competitions", produces = {MediaType.APPLICATION_JSON_VALUE})
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RestController
public class CompetitionsController {
    private final Logger log = LoggerFactory.getLogger(CompetitionsController.class);
    private DbCompetitionsRepository competitionsRepository;
    private DbUserRepository userRepository;
    private DbTeamsRepository teamsRepository;
    private IEntitiesMapper<NewCompetition, DbCompetition> competitionMapper;
    private ICompetitionPinGenerator pinGenerator;
    private IAddTeamToCompetitionService addTeamToCompetitionService;
    private ITeamConnectionNotifyService teamConnectionNotifyService;
    private ITeamJoinService teamJoinService;
    private IEntitiesMapper<DbCompetition, CompetitionCloneInfoResponse> competitionInfoMapper;
    private ICompetitionResultsFormatter resultsFormatter;
    private IEntityUpdater<DbCompetition, NewCompetition> competitionEntityUpdater;
    private IPageableCompetitionService pageableCompetitionService;

    public CompetitionsController(@Autowired DbCompetitionsRepository repository,
                                  @Autowired DbUserRepository userRepository,
                                  @Autowired DbTeamsRepository teamsRepository,
                                  @Autowired IEntitiesMapper<NewCompetition, DbCompetition> mapper,
                                  @Autowired ICompetitionPinGenerator pinGenerator,
                                  @Autowired IAddTeamToCompetitionService addTeamToCompetitionService,
                                  @Autowired ITeamConnectionNotifyService teamConnectionNotifyService,
                                  @Autowired ITeamJoinService teamJoinService,
                                  @Autowired IEntitiesMapper<DbCompetition, CompetitionCloneInfoResponse> competitionInfoMapper,
                                  @Autowired IPageableCompetitionService pageableCompetitionService,
                                  @Autowired ICompetitionResultsFormatter resultsFormatter,
                                  @Autowired IEntityUpdater<DbCompetition, NewCompetition> competitionUpdater) {
        this.competitionsRepository = repository;
        this.userRepository = userRepository;
        this.teamsRepository = teamsRepository;
        this.competitionMapper = mapper;
        this.pinGenerator = pinGenerator;
        this.addTeamToCompetitionService = addTeamToCompetitionService;
        this.teamConnectionNotifyService = teamConnectionNotifyService;
        this.teamJoinService = teamJoinService;
        this.competitionInfoMapper = competitionInfoMapper;
        this.resultsFormatter = resultsFormatter;
        this.competitionEntityUpdater = competitionUpdater;
        this.pageableCompetitionService = pageableCompetitionService;
    }

    @PostMapping(value = "/update_competition/{pin}")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> updateCompetition(@PathVariable String pin, @Valid @RequestBody NewCompetition competition) {
        log.info("POST: /api/competitions/update_competition/{}, body: {}", pin, competition);
        return competitionsRepository.findByPin(pin).flatMap(dbCompetition -> {
            return competitionEntityUpdater.update(dbCompetition, competition);
        }).map(competition1 -> {
            var compInfo = competitionInfoMapper.map(competition1, null);
            return (ResponseEntity)ResponseEntity.ok(compInfo);
        }).defaultIfEmpty(ResponseEntity.badRequest().body(ResponseMessage.of("There is no competition with such pin")));
    }

    @PostMapping(value = "/create")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> createCompetition(Mono<Principal> principalMono, @Valid @RequestBody NewCompetition competition) {
        return principalMono.map(principal -> {
            var name = principal.getName();
            log.info("POST: /api/competitions/create, email: {}, body: {}", name, competition);
            return name;
        }).flatMap(userEmail -> {
            return userRepository.findOneByEmail(userEmail);
        }).flatMap(dbUser -> {
            ArrayList<Pair<String, ?>> params = new ArrayList<Pair<String, ?>>();
            params.add(Pair.of("owner", dbUser));
            System.out.println(competition.getState());

            params.add(Pair.of("pin", pinGenerator.generate()));

            var dbCompetition = competitionMapper.map(competition, params);
            return competitionsRepository.save(dbCompetition);
        }).map(newCompetition -> {
            if (competition.getState().equalsIgnoreCase(DbCompetition.State.Registration.toString())) {
                return ResponseEntity.ok(CompetitionCreationResponse.builder().pin(newCompetition.getPin()).build());
            }
            return ResponseEntity.ok(ResponseMessage.of("Competition Created Successfully"));
        });
    }

    @GetMapping(value = "/get_clone_info/{pin}")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> getCompetitionInfo(@PathVariable String pin) {
        log.info("GET: /api/competitions/get_clone_info/{}", pin);
        return this.competitionsRepository.findByPin(pin).map(comp -> {
            var compInfo = competitionInfoMapper.map(comp, null);
            return (ResponseEntity)ResponseEntity.ok(compInfo);
        }).defaultIfEmpty(
                ResponseEntity.badRequest().body(ResponseMessage.of("No such competition with pin: " + pin))
        );
    }

    @PostMapping(value = "/create_team")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity<ResponseMessage>> joinTeam(@Valid @RequestBody NewTeam newTeam) {
        log.info("POST: /api/competitions/create_team, body: {}", newTeam);
        if (Objects.isNull(newTeam)) {
            return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of("Too short or empty name")));
        }
        return this.addTeamToCompetitionService.addTeamToCompetition(newTeam).map(team -> {
            return ResponseEntity.ok(ResponseMessage.of("Team created successfully"));
        }).onErrorResume(ex ->
                Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseMessage.of(ex.getMessage())))
        ).switchIfEmpty(Mono.defer(() -> Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of("Game with pin: " + newTeam.getCompetitionId() + " not found")))));
    }

    @PostMapping(value = "/check_pin")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity<GamePinCheckResponse>> checkIfGameExists(@Valid @RequestBody GamePinCheckRequest pinCheck) {
        log.info("POST: /api/competitions/check_pin, body: {}", pinCheck);
        return competitionsRepository.findByPin(pinCheck.getPin()).map(comp -> {
            if (comp.getState() != DbCompetition.State.Registration) {
                return ResponseEntity.ok(GamePinCheckResponse.of(false));
            }
            return ResponseEntity.ok(GamePinCheckResponse.of(true));
        }).defaultIfEmpty(ResponseEntity.ok(GamePinCheckResponse.of(false)));
    }

    @RequestMapping(value = "/team_join_events/{pin}", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    public Flux<ServerSentEvent<?>> subscribeToTeamJoinEvents(@PathVariable String pin) {
        log.info("REQUEST: /api/competitions/team_join_events/{}", pin);
        return teamConnectionNotifyService.getTeamEventForGame(pin).map(e -> ServerSentEvent.builder(e).build());
    }

    @PostMapping(value = "/join_team")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> joinTeam(Mono<Principal> principalMono, @Valid @RequestBody JoinTeamRequest joinTeamRequest) {
        var compMono = this.competitionsRepository.findByPin(joinTeamRequest.getCompetitionPin());

        var userMono = principalMono
                .map(Principal::getName)
                .flatMap(userEmail -> {
                    log.info("POST: /api/competitions/join_team, email: {}, body: {}", userEmail, joinTeamRequest);
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

    @GetMapping(value = "/competition_results/{pin}")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> competitionResults(@PathVariable String pin) {
        log.info("GET: /api/competitions/competition_results/{}", pin);
        return this.competitionsRepository.findByPin(pin).map(el -> {
            return (ResponseEntity)ResponseEntity.ok(resultsFormatter.getCompetitionResults(el));
        }).switchIfEmpty(Mono.defer(() -> {
            return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of("Competition with pin: " + pin + " not found")));
        })).onErrorResume(ex -> Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of(ex.getMessage()))));
    }


    @GetMapping(value = "/competitions_history/{start}/{amount}", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> competitionsHistory(Mono<Principal> principalMono, @PathVariable Integer start, @PathVariable Integer amount) {
        return principalMono
                .map(Principal::getName)
                .flatMapMany(email -> {
                    log.info("GET: /api/competitions/competitions_history/{}/{}, email: {}", start, amount, email);
                    return pageableCompetitionService.getByEmail(email, start, amount);
                })
                .collectList()
                .map(ResponseEntity::ok);
    }
}

