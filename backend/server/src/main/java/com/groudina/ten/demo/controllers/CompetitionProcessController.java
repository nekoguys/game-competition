package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.dto.*;
import com.groudina.ten.demo.exceptions.IllegalAnswerSubmissionException;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.services.IAnswersValidator;
import com.groudina.ten.demo.services.IGameManagementService;
import com.groudina.ten.demo.services.IStudentTeamFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.security.Principal;
import java.util.function.Function;
import java.util.function.Supplier;

@RequestMapping(path="/api/competition_process/{pin}", produces = {MediaType.APPLICATION_JSON_VALUE})
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RestController
public class CompetitionProcessController {
    private Logger log = LoggerFactory.getLogger(CompetitionProcessController.class);
    private DbTeamsRepository teamsRepository;
    private DbCompetitionsRepository competitionsRepository;
    private IGameManagementService gameManagementService;
    private IStudentTeamFinder teamFinder;
    private IAnswersValidator answersValidator;

    public CompetitionProcessController(
            @Autowired IGameManagementService gameManagementService,
            @Autowired DbCompetitionsRepository competitionsRepository,
            @Autowired DbTeamsRepository teamsRepository,
            @Autowired IStudentTeamFinder teamFinder,
            @Autowired IAnswersValidator validator
    ) {
        this.gameManagementService = gameManagementService;
        this.competitionsRepository = competitionsRepository;
        this.teamsRepository = teamsRepository;
        this.teamFinder = teamFinder;
        this.answersValidator = validator;
    }

    private <U, T> Mono<ResponseEntity> routine(Mono<U> source, Function<? super U, ? extends Mono<? extends T>> mapper,
                                                Supplier<String> success, Supplier<String> empty) {
        return source.flatMap(mapper).map(__ ->
                (ResponseEntity)ResponseEntity.ok(ResponseMessage.of(success.get()))
        ).onErrorResume((ex) -> {
            return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of(ex.getMessage())));
        }).switchIfEmpty(Mono.defer(() -> {
            return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of(empty.get())));
        }));
    }

    //TEACHER BEGIN

    @GetMapping("/start_competition")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> startCompetition(@PathVariable String pin) {
        log.info("GET: /api/competition_process/{}/start_competition", pin);
        return routine(competitionsRepository.findByPin(pin), (competition) -> {
            return gameManagementService.startCompetition(competition).thenReturn(false);
        }, () -> "Competition started successfully", () -> "Competition with pin: " + pin + " not found");
    }

    @GetMapping("/end_round")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> endRound(@PathVariable String pin) {
        log.info("GET: /api/competition_process/{}/end_round", pin);
        return routine(competitionsRepository.findByPin(pin), (competition) -> {
            return gameManagementService.endCurrentRound(competition).thenReturn(false);
        }, () -> "Round ended successfully", () -> "Competition with pin: " + pin + " not found");
    }

    @GetMapping("/start_round")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> startNewRound(@PathVariable String pin) {
        log.info("GET: /api/competition_process/{}/start_round", pin);
        return routine(competitionsRepository.findByPin(pin), (competition) -> {
            return gameManagementService.startNewRound(competition).thenReturn(false);
        }, () -> "Round has started successfully", () -> "Competition with pin: " + pin + " not found");
    }

    @PostMapping("/send_message")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> sendTeacherMessage(@PathVariable String pin, @Valid @RequestBody CompetitionMessageRequest request) {
        log.info("POST: /api/competition_process/{}/send_message, body: {}", pin, request);
        return routine(competitionsRepository.findByPin(pin), (competition) -> {
            return gameManagementService.addMessage(competition, request).thenReturn(false);
        }, () -> "Message sent successfully", () -> "Competition with pin: " + pin + " not found");
    }

    @RequestMapping(value = "/answers_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('TEACHER')")
    public Flux<ServerSentEvent<?>> getTeamsAnswers(@PathVariable String pin) {
        log.info("REQUEST: /api/competition_process/{}/answers_stream", pin);
        return competitionsRepository.findByPin(pin)
                .flatMapMany(comp -> gameManagementService.teamsAnswersEvents(comp))
                .map(dto -> {
                    return ServerSentEvent.builder().data(dto).build();
                });
    }

    @RequestMapping(value = "/results_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('TEACHER')")
    public Flux<ServerSentEvent<?>> getResultsEvents(@PathVariable String pin) {
        log.info("REQUEST: /api/competition_process/{}/results_stream", pin);
        return competitionsRepository.findByPin(pin)
                .flatMapMany(comp -> gameManagementService.getRoundResultsEvents(comp))
                .map(roundTeamResultDto -> ServerSentEvent.builder(roundTeamResultDto).build());
    }

    @RequestMapping(value = "/prices_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getPricesEvents(@PathVariable String pin) {
        log.info("REQUEST: /api/competition_process/{}/prices_stream", pin);
        return competitionsRepository.findByPin(pin)
                .flatMapMany(comp -> gameManagementService.getRoundPricesEvents(comp))
                .map(dto -> ServerSentEvent.builder(dto).build());
    }

    @GetMapping(value = "/comp_info", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> getCompetitionInfoForResultsTable(@PathVariable String pin) {
        log.info("GET: /api/competition_process/{}/comp_info", pin);
        return competitionsRepository.findByPin(pin)
                .flatMap(comp -> {
                    var dto = CompetitionInfoForResultsTableDto.builder()
                            .connectedTeamsCount(comp.getTeams().size())
                            .roundsCount(comp.getParameters().getRoundsCount())
                            .name(comp.getParameters().getName())
                            .build();
                    return Mono.just((ResponseEntity) ResponseEntity.ok(dto));
                }).switchIfEmpty(Mono.defer(() -> {
                    return Mono.just(ResponseEntity.badRequest().body(ResponseMessage.of("No competition with such pin")));
                }));
    }

    @RequestMapping(value="/bans", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getBanEvents(@PathVariable String pin) {
        log.info("Request: /api/competition_process/{}/bans", pin);
        return competitionsRepository.findByPin(pin)
                .flatMapMany(comp -> gameManagementService.getBannedTeamEvents(comp))
                .map(dto -> ServerSentEvent.builder(dto).build());
    }

    //TEACHER END
    //STUDENT BEGIN

    @GetMapping(value = "/student_comp_info")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> getStudentCompetitionInfo(Mono<Principal> principalMono, @PathVariable String pin) {
        log.info("GET: /api/competition_process/{}/student_comp_info", pin);
        return Mono.zip(principalMono, competitionsRepository.findByPin(pin))
                .flatMap(tuple -> {
                    var competition = tuple.getT2();
                    var email = tuple.getT1().getName();
                    DbCompetition.Parameters parameters = competition.getParameters();

                    var team = teamFinder.findTeamForStudent(competition, email);

                    if (team.isEmpty()) {
                        return Mono.empty();
                    }

                    var dto = CompetitionInfoForStudentResultsTableDto.builder()
                            .isCaptain(team.get().getCaptain().getEmail().equals(email))
                            .description(parameters.getInstruction())
                            .name(parameters.getName())
                            .roundsCount(parameters.getRoundsCount())
                            .teamName(team.get().getName())
                            .shouldShowResultTableInEnd(parameters.isShouldShowResultTableInEnd())
                            .shouldShowResultTable(parameters.isShouldShowStudentPreviousRoundResults())
                            .teamIdInGame(team.get().getIdInGame())
                            .build();
                    return Mono.just((ResponseEntity) ResponseEntity.ok(dto));
                }).switchIfEmpty(Mono.defer(() -> {
                    return Mono.just((ResponseEntity)ResponseEntity.badRequest().body(ResponseMessage.of("No competition with such pin")));
                }));
    }

    @PostMapping("/submit_answer")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> submitAnswer(Mono<Principal> principalMono, @PathVariable String pin, @Valid @RequestBody CompetitionAnswerRequestDto answerDto) {
        var comp = competitionsRepository.findByPin(pin);

        var competitionAndTeamMono = Mono.zip(
                comp,
                principalMono
        );

        return routine(competitionAndTeamMono, (tuple) -> {
            var validationResult = answersValidator.validateAnswer(IAnswersValidator.AnswerValidationRequestDto.builder().answer(answerDto.getAnswer()).build());

            if (!validationResult.isOk()) {
                return Mono.error(new IllegalAnswerSubmissionException(validationResult.getMessage()));
            }

            DbCompetition competition = tuple.getT1();
            String submitterEmail = tuple.getT2().getName();
            log.info("POST: /api/competition_process/{}/submit_answer, email: {}, body: {}", pin, submitterEmail, answerDto);

            var team = teamFinder.findTeamForStudent(competition, submitterEmail);

            if (team.isEmpty()) {
                return Mono.error(new IllegalAnswerSubmissionException("User with email: " + submitterEmail + " has no team"));
            }
            if (team.get().isBanned()) {
                return Mono.error(new IllegalAnswerSubmissionException("Your team is banned"));
            }

            if (team.get().getCaptain().getEmail().equals(submitterEmail)) {
                return gameManagementService.submitAnswer(competition, team.get(), answerDto.getAnswer(), answerDto.getRoundNumber()).thenReturn(false);
            } else {
                return Mono.error(new IllegalAnswerSubmissionException("User with email: " + submitterEmail + " is not team \"" + team.get().getName() + "\" captain"));
            }
        }, () -> "Answer submitted successfully", () -> "Competition with pin: " + pin + " not found");
    }

    @RequestMapping(value = "/messages_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getCompetitionMessages(@PathVariable String pin) {
        log.info("REQUEST: /api/competition_process/{}/messages_stream", pin);
        return competitionsRepository.findByPin(pin).flatMapMany(comp -> gameManagementService.getCompetitionMessages(comp))
                .map(e -> ServerSentEvent.builder().data(e).build());
    }

    @RequestMapping(value = "/rounds_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getCompetitionRoundEvents(@PathVariable String pin) {
        log.info("REQUEST: /api/competition_process/{}/rounds_stream", pin);
        return competitionsRepository.findByPin(pin).flatMapMany(comp -> gameManagementService.beginEndRoundEvents(comp))
                .map(e -> ServerSentEvent.builder().data(e).build());
    }

    @RequestMapping(value = "/my_answers_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getMyTeamAnswersEvents(Mono<Principal> principalMono, @PathVariable String pin) {
        return Mono.zip(principalMono, competitionsRepository.findByPin(pin))
                .flatMapMany(tuple -> {
                    var comp = tuple.getT2();
                    var email = tuple.getT1().getName();
                    var team = this.teamFinder.findTeamForStudent(comp, email);
                    log.info("REQUEST: /api/competition_process/{}/my_answers_stream, email: {}", pin, email);

                    if (team.isEmpty()) {
                        return Flux.empty();
                    }

                    return gameManagementService.teamsAnswersEvents(comp).filter(roundTeamAnswerDto -> {
                        return roundTeamAnswerDto.getTeamIdInGame() == team.get().getIdInGame();
                    });
                }).map(e -> ServerSentEvent.builder().data(e).build());
    }

    @RequestMapping(value = "/my_results_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getMyTeamResultsEvents(Mono<Principal> principalMono, @PathVariable String pin) {
        return Mono.zip(principalMono, competitionsRepository.findByPin(pin))
                .flatMapMany(tuple -> {
                    var comp = tuple.getT2();
                    var email = tuple.getT1().getName();
                    var team = this.teamFinder.findTeamForStudent(comp, email);

                    log.info("REQUEST: /api/competition_process/{}/my_results_stream, email: {}", pin, email);

                    if (team.isEmpty()) {
                        return Flux.empty();
                    }

                    return gameManagementService.getRoundResultsEvents(comp)
                            .filter(roundTeamResultDto -> {
                                return roundTeamResultDto.getTeamIdInGame() == team.get().getIdInGame();
                            });
                }).map(e -> ServerSentEvent.builder(e).build());
    }

}
