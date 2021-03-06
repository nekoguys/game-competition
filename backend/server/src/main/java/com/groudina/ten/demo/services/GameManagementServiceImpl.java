package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.*;
import com.groudina.ten.demo.dto.*;
import com.groudina.ten.demo.exceptions.*;
import com.groudina.ten.demo.models.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GameManagementServiceImpl implements IGameManagementService {
    private static final Logger logger = LoggerFactory.getLogger(GameManagementServiceImpl.class);
    private final int secondsOffset = 2;
    private DbCompetitionsRepository competitionsRepository;
    private DbCompetitionRoundInfosRepository roundInfosRepository;
    private DbCompetitionProcessInfosRepository processInfosRepository;
    private DbAnswersRepository answersRepository;
    private DbCompetitionMessagesRepository messagesRepository;
    private IRoundResultsCalculator roundResultsCalculator;
    private DbRoundResultElementsRepository roundResultElementsRepository;
    private DbTeamsRepository teamsRepository;
    private IEndRoundsScheduler endRoundsScheduler;

    private Map<String, Flux<RoundTeamAnswerDto>> teamAnswersStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<RoundTeamAnswerDto>> teamAnswersSinks = new ConcurrentHashMap<>();

    private Map<String, Flux<ITypedEvent>> beginEndRoundEventsStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<ITypedEvent>> beginEndRoundEventsSinks = new ConcurrentHashMap<>();

    private Map<String, Flux<CompetitionMessageDto>> messagesStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<CompetitionMessageDto>> messagesSinks = new ConcurrentHashMap<>();

    private Map<String, Flux<RoundTeamResultDto>> teamResultsStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<RoundTeamResultDto>> teamResultsSinks = new ConcurrentHashMap<>();

    private Map<String, Flux<PriceInRoundDto>> roundPricesStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<PriceInRoundDto>> roundPricesSinks = new ConcurrentHashMap<>();

    private Map<String, Flux<TeamBanEventDto>> teamBanStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<TeamBanEventDto>> teamBanSinks = new ConcurrentHashMap<>();

    public GameManagementServiceImpl(
            @Autowired DbCompetitionsRepository competitionsRepository,
            @Autowired DbCompetitionRoundInfosRepository roundInfosRepository,
            @Autowired DbCompetitionProcessInfosRepository processInfosRepository,
            @Autowired DbAnswersRepository answersRepository,
            @Autowired DbCompetitionMessagesRepository messagesRepository,
            @Autowired DbRoundResultElementsRepository roundResultElementsRepository,
            @Autowired IRoundResultsCalculator roundResultsCalculator,
            @Autowired DbTeamsRepository teamsRepository,
            @Autowired IEndRoundsScheduler endRoundsScheduler
    ) {
        this.competitionsRepository = competitionsRepository;
        this.roundInfosRepository = roundInfosRepository;
        this.processInfosRepository = processInfosRepository;
        this.answersRepository = answersRepository;
        this.messagesRepository = messagesRepository;
        this.roundResultElementsRepository = roundResultElementsRepository;
        this.roundResultsCalculator = roundResultsCalculator;
        this.teamsRepository = teamsRepository;
        this.endRoundsScheduler = endRoundsScheduler;
    }

    @PostConstruct
    private void initScheduler() {
        this.competitionsRepository.findAllByParameters_IsAutoRoundEndingTrueAndState(DbCompetition.State.InProcess).subscribe(el -> {
            if (el.getCompetitionProcessInfo().getCurrentRoundNumber() != 0) {
                this.scheduleRoundEnd(el).subscribe();
            }
        });
    }

    private Flux<TeamBanEventDto> createTeamBanProcessor(DbCompetition competition) {
        String pin = competition.getPin();
        var processor = ReplayProcessor.<TeamBanEventDto>create().serialize();

        teamBanSinks.computeIfAbsent(pin, (__) -> {
            var sink = processor.sink();

            competition.getTeams().forEach(team -> {
                if (team.isBanned()) {
                    sink.next(TeamBanEventDto.builder()
                            .teamIdInGame(team.getIdInGame())
                            .teamName(team.getName())
                            .round(team.getBanRound())
                            .build()
                    );
                }
            });

            return sink;
        });


        return processor;
    }

    private Flux<PriceInRoundDto> createPricesProcessor(DbCompetition competition) {
        String pin = competition.getPin();
        var processor = ReplayProcessor.<PriceInRoundDto>create().serialize();

        roundPricesSinks.computeIfAbsent(pin, (__) -> {
            var sink = processor.sink();

            var roundInfos = competition.getCompetitionProcessInfo().getRoundInfos();
            for (int roundNumber = 0; roundNumber < roundInfos.size(); ++roundNumber) {
                if (roundInfos.get(roundNumber).isEnded()) {
                    sink.next(PriceInRoundDto.builder()
                            .price(roundInfos.get(roundNumber).getPrice())
                            .roundNumber(roundNumber + 1)
                            .build());
                }
            }

            return sink;
        });

        return processor;
    }

    private Flux<RoundTeamResultDto> createTeamResultsProcessor(DbCompetition competition) {
        String pin = competition.getPin();
        var processor = ReplayProcessor.<RoundTeamResultDto>create().serialize();

        teamResultsSinks.computeIfAbsent(pin, (__) -> {
            var sink = processor.sink();

            var roundInfos = competition.getCompetitionProcessInfo().getRoundInfos();
            for (int roundNumber = 0; roundNumber < roundInfos.size(); ++roundNumber) {
                int finalRoundNumber = roundNumber + 1;
                roundInfos.get(roundNumber).getRoundResult().forEach(roundResultElement -> {
                    var roundResultDto = RoundTeamResultDto.builder()
                            .roundNumber(finalRoundNumber)
                            .income(roundResultElement.getIncome())
                            .teamIdInGame(roundResultElement.getTeam().getIdInGame())
                            .build();
                    sink.next(roundResultDto);
                });
            }
            return sink;
        });

        return processor;
    }

    private Flux<CompetitionMessageDto> createMessagesProcessor(DbCompetition competition) {
        String pin = competition.getPin();
        var processor = ReplayProcessor.<CompetitionMessageDto>create().serialize();

        messagesSinks.computeIfAbsent(pin, (__) -> {
            var sink = processor.sink();

            competition.getCompetitionProcessInfo().getMessages().forEach(message -> {
                sink.next(
                        CompetitionMessageDto
                                .builder()
                                .message(message.getMessage())
                                .sendTime(message.getSendTime().atOffset(ZoneOffset.UTC).toEpochSecond())
                                .build()
                );
            });
           return sink;
        });

        return processor;
    }

    private Flux<RoundTeamAnswerDto> createTeamAnswersProcessor(DbCompetition competition) {
        String pin = competition.getPin();
        var processor = ReplayProcessor.<RoundTeamAnswerDto>create().serialize();

        teamAnswersSinks.computeIfAbsent(pin, (__) -> {
            var sink = processor.sink();

            var roundInfos = competition.getCompetitionProcessInfo().getRoundInfos();
            for (int i = 0; i < roundInfos.size(); ++i) {
                var roundInfo = roundInfos.get(i);

                int finalI = i;
                roundInfo.getAnswerList().forEach((answer) -> {
                    sink.next(
                            RoundTeamAnswerDto
                                    .builder()
                                    .roundNumber(finalI + 1)
                                    .teamAnswer(answer.getValue())
                                    .teamIdInGame(answer.getSubmitter().getIdInGame())
                                    .build()
                    );
                });
            }
            return sink;
        });

        return processor;
    }

    private int findRoundLength(int roundNumber, DbCompetition.Parameters.RoundsLengthHistory history) {
        int roundLength = 0;

        for (int ind = 0; ind < history.getRoundNumbers().size(); ++ind) {
            if (history.getRoundNumbers().get(ind) <= roundNumber) {
                roundLength = history.getRoundLength().get(ind);
            }
        }

        return roundLength;
    }

    private Flux<ITypedEvent> createBeginEndRoundsProcessor(DbCompetition competition) {
        String pin = competition.getPin();
        var processor = ReplayProcessor.<ITypedEvent>create(1).serialize();

        beginEndRoundEventsSinks.computeIfAbsent(pin, (__) -> {
            var sink = processor.sink();

            if (Objects.isNull(competition.getCompetitionProcessInfo())) {
                return sink;
            }

            var currentRound = competition.getCompetitionProcessInfo().getCurrentRoundNumber();

            if (currentRound != 0) {
                var lastRoundInfo = competition.getCompetitionProcessInfo().getCurrentRound();
                var history = competition.getParameters().getRoundsLengthHistory();
                int roundLength = findRoundLength(currentRound, history);

                if (!lastRoundInfo.isEnded()) {

                    sink.next(
                            NewRoundEventDto
                                    .builder()
                                    .beginTime(lastRoundInfo.getStartTime().atOffset(ZoneOffset.UTC).toEpochSecond())// IMPORTANT
                                    .roundLength(roundLength + lastRoundInfo.getAdditionalMinutes() * 60)
                                    .roundNumber(currentRound)
                                    .build()
                    );
                } else {
                    sink.next(
                            EndRoundEventDto
                                    .builder()
                                    .isEndOfGame(currentRound == competition.getParameters().getRoundsCount())
                                    .roundNumber(currentRound)
                                    .roundLength(roundLength)
                                    .build()
                    );
                }
            }

            return sink;
        });

        return processor;
    }

    @Override
    public Mono<Void> startCompetition(DbCompetition competition) {
        if (competition.getState() != DbCompetition.State.Registration) {
            return Mono.error(new RuntimeException("Illegal Competition State"));
        }

        competition.setState(DbCompetition.State.InProcess);
        DbCompetitionProcessInfo processInfo = DbCompetitionProcessInfo.builder().currentRoundNumber(0).build();

        return processInfosRepository.save(processInfo).flatMap((savedProcessInfo) -> {
            competition.setCompetitionProcessInfo(savedProcessInfo);
            return competitionsRepository.save(competition);
        }).doOnNext(el -> {
            var event = EndRoundEventDto.builder()
                    .roundNumber(0)
                    .roundLength(el.getParameters().getRoundLengthInSeconds())
                    .isEndOfGame(false)
                    .build();
            var pin = competition.getPin();
            this.beginEndRoundEventsStorage.compute(pin, (__, before) -> {
                if (Objects.isNull(before)) {
                    var flux = createBeginEndRoundsProcessor(el);
                    beginEndRoundEventsSinks.get(pin).next(event);
                    return flux;
                } else {
                    beginEndRoundEventsSinks.get(pin).next(event);
                    return before;
                }
            });
        }).then();
    }

    private Mono<Void> scheduleRoundEnd(DbCompetition competition) {
        int currentNumber = competition.getCompetitionProcessInfo().getCurrentRoundNumber();
        DbCompetitionRoundInfo currentRound = competition.getCompetitionProcessInfo().getCurrentRound();

        return this.endRoundsScheduler.submitRoundForScheduler(competition, currentRound,
                findRoundLength(currentNumber, competition.getParameters().getRoundsLengthHistory()))
                .flatMap(el -> {
                    return competitionsRepository.findByPin(competition.getPin()).flatMap(savedCompetition -> {
                        return this.endCurrentRound(savedCompetition).thenReturn(1).flatMap(__ -> {
                            this.endRoundsScheduler.removeRoundFromScheduler(savedCompetition, savedCompetition.getCompetitionProcessInfo().getCurrentRound());

                            if (savedCompetition.getParameters().getRoundsCount() != savedCompetition.getCompetitionProcessInfo().getCurrentRoundNumber()) {
                                return this.startNewRound(savedCompetition);
                            } else {
                                return Mono.just(1).then();
                            }
                        });
                    });
                });
    }

    @Override
    public Mono<Void> addMinuteToCurrentRound(DbCompetition competition) {
        if (competition.getCompetitionProcessInfo().getCurrentRoundNumber() == 0) {
            return Mono.error(new RoundLengthIncreaseInNotStartedCompException("Tried to add minute to current round but" +
                    "there is no current round, game rounds are empty"));
        }
        var round = competition.getCompetitionProcessInfo().getCurrentRound();
        round.addOneMinute();

        return competitionsRepository.save(competition).map(savedComp -> {
            var savedRound = savedComp.getCompetitionProcessInfo().getCurrentRound();
            var event = NewRoundEventDto
                    .builder()
                    .beginTime(savedRound.getStartTime().atOffset(ZoneOffset.UTC).toEpochSecond())// IMPORTANT
                    .roundLength(savedComp.getParameters().getRoundLengthInSeconds() + savedRound.getAdditionalMinutes() * 60)
                    .roundNumber(savedComp.getCompetitionProcessInfo().getCurrentRoundNumber())
                    .build();
            beginEndRoundEventsStorage.compute(savedComp.getPin(), (pin, prev) -> {
                if (Objects.isNull(prev)) {
                    return createBeginEndRoundsProcessor(savedComp);
                } else {
                    beginEndRoundEventsSinks.get(pin).next(event);
                    return prev;
                }
            });

            return savedComp;
        }).then();
    }

    @Override
    public Mono<Void> submitAnswer(DbCompetition competition, DbTeam team, int answer, int roundNumber) {
        if (competition.getState() != DbCompetition.State.InProcess) {
            return Mono.error(new IllegalAnswerSubmissionException("Tried to submit in not started game"));
        }
        if (competition.getCompetitionProcessInfo().getCurrentRoundNumber() != roundNumber) {
            return Mono.error(new IllegalAnswerSubmissionException("Tried to submit answer in invalid round"));
        }

        var round = competition.getCompetitionProcessInfo().getCurrentRound();

        if (round.isEnded()) {
            return Mono.error(new IllegalAnswerSubmissionException("Tried to submit in ended round"));
        }

        // TODO add after integrating auto-ending round

//        var currTimeInSeconds = LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond();
//        var approxRoundEndSeconds = round.getStartTime().plusSeconds(round.getAdditionalMinutes() * 60).atOffset(ZoneOffset.UTC).toEpochSecond();
//
//        if (approxRoundEndSeconds + secondsOffset >= currTimeInSeconds) {
//            return Mono.error(new IllegalAnswerSubmissionException("Tried to submit answer in ended round"));
//        }


        var dbTeamAnswer = DbAnswer.builder().submitter(team).value(answer).build();
        var prevAnswer = round.getAnswerList().stream()
                .filter((checkingTeam) ->
                        checkingTeam.getSubmitter().getId().equals(team.getId())
                ).findAny();

        var prevStep = Mono.just(1).then();

        if (prevAnswer.isPresent()) {
            prevStep = answersRepository.delete(prevAnswer.get()).then();
        }

        return prevStep.then(
                answersRepository.save(dbTeamAnswer).map((savedAnswer) -> {
                    competition.getCompetitionProcessInfo().getCurrentRound().addAnswer(savedAnswer);
                    return savedAnswer;
                }).then(roundInfosRepository.save(competition.getCompetitionProcessInfo().getCurrentRound()))
                        .then(processInfosRepository.save(competition.getCompetitionProcessInfo()))
                        .then(competitionsRepository.save(competition).map(savedCompetition -> {
                            String pin = savedCompetition.getPin();
                            teamAnswersStorage.compute(pin, (__, before) -> {
                                if (before == null)
                                    return createTeamAnswersProcessor(savedCompetition);
                                else {
                                    teamAnswersSinks.get(pin).next(
                                            RoundTeamAnswerDto
                                                    .builder()
                                                    .teamIdInGame(team.getIdInGame())
                                                    .teamAnswer(answer)
                                                    .roundNumber(savedCompetition.getCompetitionProcessInfo().getCurrentRoundNumber())
                                                    .build()
                                    );
                                    return before;
                                }
                            });
                            return savedCompetition;
                        }))
        ).then();
    }

    @Override
    public Flux<RoundTeamAnswerDto> teamsAnswersEvents(DbCompetition competition) {
        return teamAnswersStorage.computeIfAbsent(competition.getPin(), (pin) -> {
            return createTeamAnswersProcessor(competition);
        });
    }

    @Override
    public Flux<ITypedEvent> beginEndRoundEvents(DbCompetition competition) {
        return beginEndRoundEventsStorage.computeIfAbsent(competition.getPin(), (pin) -> {
            return createBeginEndRoundsProcessor(competition);
        });
    }

    @Override
    public Flux<CompetitionMessageDto> getCompetitionMessages(DbCompetition competition) {
        return messagesStorage.computeIfAbsent(competition.getPin(), (pin) -> {
            return createMessagesProcessor(competition);
        });
    }

    @Override
    public Flux<RoundTeamResultDto> getRoundResultsEvents(DbCompetition competition) {
        return teamResultsStorage.computeIfAbsent(competition.getPin(), (pin) -> {
            return createTeamResultsProcessor(competition);
        });
    }

    @Override
    public Flux<PriceInRoundDto> getRoundPricesEvents(DbCompetition competition) {
        return roundPricesStorage.computeIfAbsent(competition.getPin(), (pin) -> {
            return createPricesProcessor(competition);
        });
    }

    @Override
    public Flux<TeamBanEventDto> getBannedTeamEvents(DbCompetition competition) {
        return teamBanStorage.computeIfAbsent(competition.getPin(), (pin) -> {
            return createTeamBanProcessor(competition);
        });
    }

    @Override
    public Mono<Void> endCurrentRound(DbCompetition competition) {
        if (competition.getState() != DbCompetition.State.InProcess) {
            return Mono.error(new RoundEndInNotStartedCompetitionException("Attempt to end round but game not started"));
        }
        int currentRoundNumber = competition.getCompetitionProcessInfo().getCurrentRoundNumber();
        var lastRound = competition.getCompetitionProcessInfo().getCurrentRound();
        lastRound.setEnded(true);

        if (currentRoundNumber == competition.getParameters().getRoundsCount()) {
            competition.setState(DbCompetition.State.Ended);
        }

        var results = roundResultsCalculator.calculateResults(lastRound, competition);

        /// TODO ask teacher about banning and ban only then
        var bannedTeams = this.banTeams(results.getBannedTeams(), competition).flatMap(team -> {
            return this.addMessage(competition,
                    CompetitionMessageRequest.builder()
                            .message(String.format("Game: Team %d:\"%s\" is banned for exceeding loss limit", team.getIdInGame(), team.getName()))
                            .build()).thenReturn(team);
        }).collectList().doOnNext(banTeams -> {
            teamBanStorage.compute(competition.getPin(), (pin, before) -> {
                if (Objects.isNull(before)) {
                    return createTeamBanProcessor(competition);
                } else {
                    var sink = teamBanSinks.get(pin);
                    banTeams.forEach(team -> {
                        sink.next(TeamBanEventDto.builder().teamName(team.getName())
                                .round(team.getBanRound())
                                .teamIdInGame(team.getIdInGame()).build());
                    });
                    return before;
                }
            });
        });

        return bannedTeams.then(roundResultElementsRepository.saveAll(
                results.getResults()
        ).collectList().doOnNext(savedRoundResults -> {
            lastRound.addRoundResults(savedRoundResults);
            lastRound.setPrice(results.getPrice());

            roundPricesStorage.compute(competition.getPin(), (pin, before) -> {
                if (Objects.isNull(before)) {
                    return createPricesProcessor(competition);
                } else {
                    roundPricesSinks.get(pin)
                            .next(
                                    PriceInRoundDto.builder()
                                    .roundNumber(currentRoundNumber)
                                    .price(results.getPrice())
                                    .build()
                            );
                    return before;
                }
            });

            teamResultsStorage.compute(competition.getPin(), (pin, before) -> {
                if (Objects.isNull(before)) {
                    return createTeamResultsProcessor(competition);
                } else {
                    savedRoundResults.forEach(dbRoundResultElement -> {
                        teamResultsSinks.get(pin).next(
                                RoundTeamResultDto.builder()
                                        .income(dbRoundResultElement.getIncome())
                                        .roundNumber(currentRoundNumber)
                                        .teamIdInGame(dbRoundResultElement.getTeam().getIdInGame())
                                        .build()
                        );
                    });

                    return before;
                }
            });
        }))
                .then(roundInfosRepository.save(lastRound).then(competitionsRepository.save(competition)
                .doOnNext((savedCompetition) -> {
                    String pin = savedCompetition.getPin();
                    beginEndRoundEventsStorage.compute(pin, (__, before) -> {
                        if (Objects.isNull(before)) {
                            return createBeginEndRoundsProcessor(savedCompetition);
                        } else {
                            beginEndRoundEventsSinks.get(pin)
                                    .next(EndRoundEventDto.builder()
                                            .roundNumber(currentRoundNumber)
                                            .roundLength(findRoundLength(currentRoundNumber, savedCompetition.getParameters().getRoundsLengthHistory()))
                                            .isEndOfGame(currentRoundNumber == savedCompetition.getParameters().getRoundsCount())
                                            .build()
                                    );

                            return before;
                        }
                    });
                }))).then();
    }

    @Override
    public Mono<Void> startNewRound(DbCompetition competition) {
        if (competition.getState() != DbCompetition.State.InProcess) {
            return Mono.error(new IllegalGameStateException("Attempt to start round but game not in process"));
        }

        var processInfo = competition.getCompetitionProcessInfo();
        int currentRound = processInfo.getCurrentRoundNumber();

        var newRoundInfo = DbCompetitionRoundInfo
                .builder()
                .additionalMinutes(0)
                .isEnded(false)
                .startTime(LocalDateTime.now(Clock.systemUTC()))
                .build();

        return roundInfosRepository.save(newRoundInfo).flatMap((savedRoundInfo) -> {
            processInfo.addRoundInfo(savedRoundInfo);

            return processInfosRepository.save(processInfo).then(competitionsRepository.save(competition)
                    .doOnNext((savedCompetition) -> {
                        String pin = savedCompetition.getPin();
                        beginEndRoundEventsStorage.compute(pin, (__, before) -> {
                            if (Objects.isNull(before)) {
                                return createBeginEndRoundsProcessor(savedCompetition);
                            } else {
                                beginEndRoundEventsSinks.get(pin).next(
                                        NewRoundEventDto
                                                .builder()
                                                .roundNumber(currentRound + 1)
                                                .roundLength(findRoundLength(currentRound + 1, savedCompetition.getParameters().getRoundsLengthHistory()))
                                                .beginTime(LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond())
                                                .build()
                                );

                                return before;
                            }
                        });
                    }));
        }).doOnNext(el -> {
            if (el.getParameters().isAutoRoundEnding()) {
                this.scheduleRoundEnd(el).subscribe(rnd -> {
                    logger.info("Ended {} of competition with pin {} (automatic)", el.getCompetitionProcessInfo().getCurrentRound(),
                            el.getPin());
                });
            }
        }).then();

    }

    @Override
    public Mono<Void> addMessage(DbCompetition competition, CompetitionMessageRequest request) {
        var message = DbCompetitionMessage.builder().message(request.getMessage()).sendTime(LocalDateTime.now()).build();

        return messagesRepository.save(message).flatMap(savedMessage -> {
            competition.getCompetitionProcessInfo().addMessage(savedMessage);
            return Mono.zip(
                    processInfosRepository.save(competition.getCompetitionProcessInfo()).thenReturn(competition),
                    Mono.just(savedMessage)
            );
        }).doOnNext(competitionSavedMessageTuple -> {
            DbCompetitionMessage savedMessage = competitionSavedMessageTuple.getT2();
            DbCompetition savedComp = competitionSavedMessageTuple.getT1();

            String pin = savedComp.getPin();

            messagesStorage.compute(pin, (__, before) -> {
                if (Objects.isNull(before)) {
                    return createMessagesProcessor(savedComp);
                } else {
                    messagesSinks.get(pin).next(
                            CompetitionMessageDto
                                    .builder()
                                    .message(savedMessage.getMessage())
                                    .sendTime(savedMessage.getSendTime().atOffset(ZoneOffset.UTC).toEpochSecond())
                                    .build()
                    );
                    return before;
                }
            });
        }).then();
    }

    @Override
    public Mono<Void> changeRoundLength(DbCompetition competition, int newRoundLength) {
        if (newRoundLength <= 1) {
            return Mono.error(new ResponseException("Round length is too small"));
        }
        competition.getParameters().getRoundsLengthHistory().add(competition.getCompetitionProcessInfo().getCurrentRoundNumber() + 1,newRoundLength);

        return competitionsRepository.save(competition).then();
    }

    @Override
    public Mono<DbCompetition> restartGame(DbCompetition competition) {
        if (competition.getState() != DbCompetition.State.InProcess) {
            return Mono.error(new IllegalGameRestartException("Game is not in process, can't restart"));
        } else if (competition.getCompetitionProcessInfo().getCurrentRoundNumber() == 0) {
            return Mono.error(new IllegalGameRestartException("First round in game has not been started, makes no sense to restart"));
        }
        if (competition.getParameters().isAutoRoundEnding()) {
            endRoundsScheduler.removeRoundFromScheduler(competition, competition.getCompetitionProcessInfo().getCurrentRound());
        }

        int currentRound = competition.getCompetitionProcessInfo().getCurrentRoundNumber();
        competition.getCompetitionProcessInfo().setCurrentRoundNumber(0);
        //var toDeleteFlux = Mono.from(roundInfosRepository.deleteAll(competition.getCompetitionProcessInfo().getRoundInfos()));

        var answersToDelete = competition.getCompetitionProcessInfo().getRoundInfos().stream().flatMap(el -> {
            var stream = List.copyOf(el.getAnswerList()).stream();
            el.getAnswerList().clear();
            return stream;
        }).peek(el -> {
            logger.info("Deleting DbAnswer {}", el.getId());
        }).collect(Collectors.toList());
        var resultsToDelete = competition.getCompetitionProcessInfo().getRoundInfos().stream().flatMap(el -> {
            var stream = List.copyOf(el.getRoundResult()).stream();
            el.getRoundResult().clear();
            return stream;
        }).peek(el -> {
            logger.info("Deleting DbRoundResultElement {}", el.getId());
        }).collect(Collectors.toList());

        var roundInfos = List.copyOf(competition.getCompetitionProcessInfo().getRoundInfos());
        competition.getCompetitionProcessInfo().getRoundInfos().clear();

        var toDeleteFlux = Mono.zip(answersRepository.deleteAll(answersToDelete).thenReturn(1),
                        roundResultElementsRepository.deleteAll(resultsToDelete).thenReturn(1)
                ).then(
                        roundInfosRepository.deleteAll(roundInfos).thenReturn(1)
        );

        competition.getCompetitionProcessInfo().getRoundInfos().clear();

        teamAnswersSinks.computeIfPresent(competition.getPin(), (pin, before) -> {
            before.next(
                    new RoundTeamAnswerCancellationDto(-1, -1, -1,
                            new CancellationInfoDto(currentRound)//cancel all rounds results
                    ));
            return before;
        });

        teamResultsSinks.computeIfPresent(competition.getPin(), (pin, before) -> {
            before.next(
                    new RoundTeamResultCancellationDto(-1, -1, -1,
                            new CancellationInfoDto(currentRound))
            );
            return before;
        });

        roundPricesSinks.computeIfPresent(competition.getPin(), (pin, before) -> {
            before.next(
                    new PriceInRoundCancellationDto(-1, -1, new CancellationInfoDto(currentRound))
            );
            return before;
        });

        teamBanSinks.computeIfPresent(competition.getPin(), (pin, before) -> {
            before.next(
                    new TeamBanEventCancellationDto(-1, "any", -1, new CancellationInfoDto(currentRound))
            );
            return before;
        });

        beginEndRoundEventsSinks.computeIfPresent(competition.getPin(), (pin, before) -> {
            var roundNumber = competition.getCompetitionProcessInfo().getCurrentRoundNumber();
            before.next(
                    EndRoundEventDto.builder()
                            .isEndOfGame(roundNumber == competition.getParameters().getRoundsCount())
                            .roundLength(findRoundLength(roundNumber,
                                    competition.getParameters().getRoundsLengthHistory()))
                            .roundNumber(competition.getCompetitionProcessInfo().getCurrentRoundNumber())
                            .build()
            );
            return before;
        });

        var teams = competition.getTeams().stream().filter(DbTeam::isBanned).collect(Collectors.toList());
        for (var team : teams) {
            team.setBanned(false);
        }


        return toDeleteFlux
                .then(teamsRepository.saveAll(teams).collectList())
                .then(processInfosRepository.save(competition.getCompetitionProcessInfo())
        ).flatMap(savedProcessInfo -> {
            competition.getParameters().getRoundsLengthHistory().getRoundNumbers().clear();
            competition.getParameters().getRoundsLengthHistory().getRoundLength().clear();

            competition.getParameters().getRoundsLengthHistory().add(
                    competition.getCompetitionProcessInfo().getCurrentRoundNumber(),
                    competition.getParameters().getRoundLengthInSeconds()
            );

            competition.setCompetitionProcessInfo(savedProcessInfo);
            return competitionsRepository.save(competition);
        });
    }

    @Override
    public Mono<Void> clear() {
        teamAnswersStorage.clear();
        teamAnswersSinks.clear();
        messagesStorage.clear();
        messagesSinks.clear();
        beginEndRoundEventsStorage.clear();
        beginEndRoundEventsSinks.clear();
        teamResultsStorage.clear();
        teamResultsSinks.clear();
        roundPricesSinks.clear();
        roundPricesStorage.clear();
        teamBanSinks.clear();
        teamBanStorage.clear();

        return Mono.just(1).then();
    }

    private Flux<DbTeam> banTeams(List<Integer> bannedTeams, DbCompetition competition) {
        var teams = competition.getTeams().stream()
                .filter(el -> bannedTeams.contains(el.getIdInGame())).collect(Collectors.toList());
        teams.forEach(team -> {
            team.setBanned(true);
            team.setBanRound(competition.getCompetitionProcessInfo().getCurrentRoundNumber());
        });
        return teamsRepository.saveAll(teams);
    }

}
