package com.groudina.ten.demo.controllers;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.datasource.DbTeamsRepository;
import com.groudina.ten.demo.dto.*;
import com.groudina.ten.demo.exceptions.IllegalAnswerSubmissionException;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.services.IGameManagementService;
import com.groudina.ten.demo.services.IStudentTeamFinder;
import lombok.extern.log4j.Log4j2;
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

@Log4j2
@RequestMapping(path="/api/competition_process/{pin}", produces = {MediaType.APPLICATION_JSON_VALUE})
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RestController
public class CompetitionProcessController {
    private DbTeamsRepository teamsRepository;
    private DbCompetitionsRepository competitionsRepository;
    private IGameManagementService gameManagementService;
    private IStudentTeamFinder teamFinder;

    public CompetitionProcessController(
            @Autowired IGameManagementService gameManagementService,
            @Autowired DbCompetitionsRepository competitionsRepository,
            @Autowired DbTeamsRepository teamsRepository,
            @Autowired IStudentTeamFinder teamFinder
    ) {
        this.gameManagementService = gameManagementService;
        this.competitionsRepository = competitionsRepository;
        this.teamsRepository = teamsRepository;
        this.teamFinder = teamFinder;
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
        return routine(competitionsRepository.findByPin(pin), (competition) -> {
            return gameManagementService.startCompetition(competition).thenReturn(false);
        }, () -> "Competition started successfully", () -> "Competition with pin: " + pin + " not found");
    }

    @GetMapping("/end_round")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> endRound(@PathVariable String pin) {
        return routine(competitionsRepository.findByPin(pin), (competition) -> {
            return gameManagementService.endCurrentRound(competition).thenReturn(false);
        }, () -> "Round ended successfully", () -> "Competition with pin: " + pin + " not found");
    }

    @GetMapping("/start_round")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> startNewRound(@PathVariable String pin) {
        return routine(competitionsRepository.findByPin(pin), (competition) -> {
            return gameManagementService.startNewRound(competition).thenReturn(false);
        }, () -> "Round has started successfully", () -> "Competition with pin: " + pin + " not found");
    }

    @PostMapping("/send_message")
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> sendTeacherMessage(@PathVariable String pin, @Valid @RequestBody CompetitionMessageRequest request) {
        return routine(competitionsRepository.findByPin(pin), (competition) -> {
            return gameManagementService.addMessage(competition, request).thenReturn(false);
        }, () -> "Message sent successfully", () -> "Competition with pin: " + pin + " not found");
    }

    @RequestMapping(value = "/answers_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('TEACHER')")
    public Flux<ServerSentEvent<?>> getTeamsAnswers(@PathVariable String pin) {
        return competitionsRepository.findByPin(pin)
                .flatMapMany(comp -> gameManagementService.teamsAnswersEvents(comp))
                .map(dto -> {
                    return ServerSentEvent.builder().data(dto).build();
                });
    }

    @RequestMapping(value = "/results_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('TEACHER')")
    public Flux<ServerSentEvent<?>> getResultsEvents(@PathVariable String pin) {
        return competitionsRepository.findByPin(pin)
                .flatMapMany(comp -> gameManagementService.getRoundResultsEvents(comp))
                .map(roundTeamResultDto -> ServerSentEvent.builder(roundTeamResultDto).build());
    }

    @RequestMapping(value = "/prices_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getPricesEvents(@PathVariable String pin) {
        return competitionsRepository.findByPin(pin)
                .flatMapMany(comp -> gameManagementService.getRoundPricesEvents(comp))
                .map(dto -> ServerSentEvent.builder(dto).build());
    }

    @GetMapping(value = "/comp_info", produces = {MediaType.APPLICATION_JSON_VALUE})
    @PreAuthorize("hasRole('TEACHER')")
    public Mono<ResponseEntity> getCompetitionInfoForResultsTable(@PathVariable String pin) {
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

    //TEACHER END
    //STUDENT BEGIN

    @GetMapping(value = "/student_comp_info")
    @PreAuthorize("hasRole('STUDENT')")
    public Mono<ResponseEntity> getStudentCompetitionInfo(Mono<Principal> principalMono, @PathVariable String pin) {
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
            DbCompetition competition = tuple.getT1();
            String submitterEmail = tuple.getT2().getName();
            var team = teamFinder.findTeamForStudent(competition, submitterEmail);

            if (team.isEmpty()) {
                return Mono.error(new IllegalAnswerSubmissionException("User with email: " + submitterEmail + " has no team"));
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
        return competitionsRepository.findByPin(pin).flatMapMany(comp -> gameManagementService.getCompetitionMessages(comp))
                .map(e -> ServerSentEvent.builder().data(e).build());
    }

    @RequestMapping(value = "/rounds_stream", produces = {MediaType.TEXT_EVENT_STREAM_VALUE})
    @PreAuthorize("hasRole('STUDENT')")
    public Flux<ServerSentEvent<?>> getCompetitionRoundEvents(@PathVariable String pin) {
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
