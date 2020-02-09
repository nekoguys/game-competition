package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.dto.EndRoundEventDto;
import com.groudina.ten.demo.dto.NewRoundEventDto;
import com.groudina.ten.demo.dto.RoundTeamAnswerDto;
import com.groudina.ten.demo.models.DbAnswer;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbCompetitionProcessInfo;
import com.groudina.ten.demo.models.DbTeam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import reactor.core.publisher.*;

import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GameManagementServiceImpl implements IGameManagementService {
    private DbCompetitionsRepository competitionsRepository;

    private Map<String, Flux<RoundTeamAnswerDto>> teamAnswersStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<RoundTeamAnswerDto>> teamAnswersSinks = new ConcurrentHashMap<>();

    private Map<String, Flux<EndRoundEventDto>> endRoundEventsStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<EndRoundEventDto>> endRoundEventsSinks = new ConcurrentHashMap<>();

    private Map<String, Flux<NewRoundEventDto>> newRoundEventsStorage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<NewRoundEventDto>> newRoundEventsSinks = new ConcurrentHashMap<>();


    public GameManagementServiceImpl(
            @Autowired DbCompetitionsRepository competitionsRepository
    ) {
        this.competitionsRepository = competitionsRepository;
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

    private Flux<NewRoundEventDto> createNewRoundsProcessor(DbCompetition competition) {
        String pin = competition.getPin();
        var processor = ReplayProcessor.<NewRoundEventDto>create(1).serialize();

        newRoundEventsSinks.computeIfAbsent(pin, (__) -> {
            var sink = processor.sink();

            var currentRound = competition.getCompetitionProcessInfo().getCurrentRoundNumber();

            var lastRoundInfo = competition.getCompetitionProcessInfo().getCurrentRound();

            sink.next(
                    NewRoundEventDto
                            .builder()
                            .beginTime(lastRoundInfo.getStartTime().atOffset(ZoneOffset.UTC).toEpochSecond())// IMPORTANT
                            .roundLength(competition.getParameters().getRoundLengthInSeconds() + lastRoundInfo.getAdditionalMinutes() * 60)
                            .roundNumber(currentRound)
                            .build()
            );

            return sink;
        });

        return processor;
    }

    private Flux<EndRoundEventDto> createEndRoundsProcessor(DbCompetition competition) {
        String pin = competition.getPin();
        var processor = DirectProcessor.<EndRoundEventDto>create().serialize();

        endRoundEventsSinks.computeIfAbsent(pin, (__) -> {
            return processor.sink();
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
        //TODO start first round
        competition.setCompetitionProcessInfo(processInfo);

        return competitionsRepository.save(competition).then();
    }

    @Override
    public Mono<Void> addMinuteToCurrentRound(DbCompetition competition) {
        var round = competition.getCompetitionProcessInfo().getCurrentRound();
        round.addOneMinute();

        return competitionsRepository.save(competition).map(savedComp -> {
            var event = NewRoundEventDto
                    .builder()
                    .beginTime(round.getStartTime().atOffset(ZoneOffset.UTC).toEpochSecond())// IMPORTANT
                    .roundLength(savedComp.getParameters().getRoundLengthInSeconds() + round.getAdditionalMinutes() * 60)
                    .roundNumber(savedComp.getCompetitionProcessInfo().getCurrentRoundNumber())
                    .build();
            newRoundEventsStorage.compute(savedComp.getPin(), (pin, prev) -> {
                if (Objects.isNull(prev)) {
                    return createNewRoundsProcessor(savedComp);
                } else {
                    newRoundEventsSinks.get(pin).next(event);
                    return prev;
                }
            });

            return savedComp;
        }).then();
    }

    @Override
    public Mono<Void> submitAnswer(DbCompetition competition, DbTeam team, int answer) {
        competition.getCompetitionProcessInfo().getCurrentRound().addAnswer(DbAnswer.builder().submitter(team).value(answer).build());

        return competitionsRepository.save(competition).map(savedCompetition -> {
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
        ).then();
    }

    @Override
    public Flux<RoundTeamAnswerDto> teamsAnswersEvents(DbCompetition competition) {
        return teamAnswersStorage.get(competition.getPin());
    }

    @Override
    public Flux<EndRoundEventDto> getEndRoundEvents(DbCompetition competition) {
        return endRoundEventsStorage.get(competition.getPin());
    }

    @Override
    public Flux<NewRoundEventDto> getNewRoundEvents(DbCompetition competition) {
        return newRoundEventsStorage.get(competition.getPin());
    }
}
