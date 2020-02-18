package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.*;
import com.groudina.ten.demo.dto.*;
import com.groudina.ten.demo.exceptions.CompetitionRoundsOverflowException;
import com.groudina.ten.demo.exceptions.IllegalAnswerSubmissionException;
import com.groudina.ten.demo.exceptions.RoundEndInNotStartedCompetitionException;
import com.groudina.ten.demo.exceptions.RoundLengthIncreaseInNotStartedCompException;
import com.groudina.ten.demo.models.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ReplayProcessor;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GameManagementServiceImpl implements IGameManagementService {
    private final int secondsOffset = 2;
    private DbCompetitionsRepository competitionsRepository;
    private DbCompetitionRoundInfosRepository roundInfosRepository;
    private DbCompetitionProcessInfosRepository processInfosRepository;
    private DbAnswersRepository answersRepository;
    private DbCompetitionMessagesRepository messagesRepository;

    private Map<String, Flux<RoundTeamAnswerDto>> teamAnswersStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<RoundTeamAnswerDto>> teamAnswersSinks = new ConcurrentHashMap<>();

    private Map<String, Flux<ITypedEvent>> beginEndRoundEventsStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<ITypedEvent>> beginEndRoundEventsSinks = new ConcurrentHashMap<>();

    private Map<String, Flux<CompetitionMessageDto>> messagesStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<CompetitionMessageDto>> messagesSinks = new ConcurrentHashMap<>();

    public GameManagementServiceImpl(
            @Autowired DbCompetitionsRepository competitionsRepository,
            @Autowired DbCompetitionRoundInfosRepository roundInfosRepository,
            @Autowired DbCompetitionProcessInfosRepository processInfosRepository,
            @Autowired DbAnswersRepository answersRepository,
            @Autowired DbCompetitionMessagesRepository messagesRepository
    ) {
        this.competitionsRepository = competitionsRepository;
        this.roundInfosRepository = roundInfosRepository;
        this.processInfosRepository = processInfosRepository;
        this.answersRepository = answersRepository;
        this.messagesRepository = messagesRepository;
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

    private Flux<ITypedEvent> createBeginEndRoundsProcessor(DbCompetition competition) {
        String pin = competition.getPin();
        var processor = ReplayProcessor.<ITypedEvent>create(1).serialize();

        beginEndRoundEventsSinks.computeIfAbsent(pin, (__) -> {
            var sink = processor.sink();

            var currentRound = competition.getCompetitionProcessInfo().getCurrentRoundNumber();

            if (currentRound != 0) {
                var lastRoundInfo = competition.getCompetitionProcessInfo().getCurrentRound();

                if (!lastRoundInfo.isEnded()) {
                    sink.next(
                            NewRoundEventDto
                                    .builder()
                                    .beginTime(lastRoundInfo.getStartTime().atOffset(ZoneOffset.UTC).toEpochSecond())// IMPORTANT
                                    .roundLength(competition.getParameters().getRoundLengthInSeconds() + lastRoundInfo.getAdditionalMinutes() * 60)
                                    .roundNumber(currentRound)
                                    .build()
                    );
                } else {
                    sink.next(
                            EndRoundEventDto
                                    .builder()
                                    .isEndOfGame(currentRound == competition.getParameters().getRoundsCount())
                                    .roundNumber(currentRound)
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

            return this.startNewRound(competition);
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
        if (Objects.isNull(competition.getCompetitionProcessInfo())) {
            return Mono.error(new IllegalAnswerSubmissionException("Tried to submit in not started game"));
        }
        if (competition.getCompetitionProcessInfo().getCurrentRoundNumber() != roundNumber) {
            return Mono.error(new IllegalAnswerSubmissionException("Tried to submit answer in invalid round"));
        }

        var currTimeInSeconds = LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond();
        var round = competition.getCompetitionProcessInfo().getCurrentRound();
        var approxRoundEndSeconds = round.getStartTime().plusSeconds(round.getAdditionalMinutes() * 60).atOffset(ZoneOffset.UTC).toEpochSecond();

        if (approxRoundEndSeconds + secondsOffset >= currTimeInSeconds) {
            return Mono.error(new IllegalAnswerSubmissionException("Tried to submit answer in ended round"));
        }


        var dbTeamAnswer = DbAnswer.builder().submitter(team).value(answer).build();

        return answersRepository.save(dbTeamAnswer).map((savedAnswer) -> {
            competition.getCompetitionProcessInfo().getCurrentRound().addAnswer(savedAnswer);
            return savedAnswer;
        }).then(competitionsRepository.save(competition).map(savedCompetition -> {
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
                }
        )).then();
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
    public Mono<Void> endCurrentRound(DbCompetition competition) {
        int currentRoundNumber = competition.getCompetitionProcessInfo().getCurrentRoundNumber();
        if (currentRoundNumber == 0) {
            return Mono.error(new RoundEndInNotStartedCompetitionException("Attempt to end round, but there is no rounds"));
        }

        var lastRound = competition.getCompetitionProcessInfo().getCurrentRound();
        lastRound.setEnded(true);


        return competitionsRepository.save(competition)
                .doOnNext((savedCompetition) -> {

                    //TODO calc results

                    String pin = savedCompetition.getPin();
                    beginEndRoundEventsStorage.compute(pin, (__, before) -> {
                        if (Objects.isNull(before)) {
                            return createBeginEndRoundsProcessor(savedCompetition);
                        } else {
                            beginEndRoundEventsSinks.get(pin)
                                    .next(EndRoundEventDto.builder()
                                            .roundNumber(currentRoundNumber)
                                            .isEndOfGame(currentRoundNumber == savedCompetition.getParameters().getRoundsCount())
                                            .build()
                                    );

                            return before;
                        }
                    });
                }).then();
    }

    @Override
    public Mono<Void> startNewRound(DbCompetition competition) {
        var processInfo = competition.getCompetitionProcessInfo();
        int currentRound = processInfo.getCurrentRoundNumber();
        if (currentRound == competition.getParameters().getRoundsCount()) {
            return Mono.error(new CompetitionRoundsOverflowException("Amount of rounds exceeded"));
        }

        var newRoundInfo = DbCompetitionRoundInfo
                .builder()
                .additionalMinutes(0)
                .isEnded(false)
                .startTime(LocalDateTime.now(Clock.systemUTC()))
                .build();

        return roundInfosRepository.save(newRoundInfo).flatMap((savedRoundInfo) -> {
            processInfo.addRoundInfo(savedRoundInfo);

            return competitionsRepository.save(competition)
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
                                                .roundLength(savedCompetition.getParameters().getRoundLengthInSeconds())
                                                .beginTime(LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond())
                                                .build()
                                );

                                return before;
                            }
                        });
                    });
        }).then();

    }

    @Override
    public Mono<Void> addMessage(DbCompetition competition, CompetitionMessageRequest request) {
        var message = DbCompetitionMessage.builder().message(request.getMessage()).sendTime(LocalDateTime.now()).build();

        return messagesRepository.save(message).flatMap(savedMessage -> {
            competition.getCompetitionProcessInfo().addMessage(savedMessage);
            return Mono.zip(competitionsRepository.save(competition), Mono.just(savedMessage));
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

}
