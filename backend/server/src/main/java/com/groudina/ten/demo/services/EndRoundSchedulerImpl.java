package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.CompetitionRoundInfoSchedulerDto;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbCompetitionRoundInfo;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class EndRoundSchedulerImpl implements IEndRoundsScheduler {
    private Map<CompetitionRoundInfoSchedulerDto, MonoProcessor<DbCompetitionRoundInfo>> processors = new ConcurrentHashMap<>();

    @Override
    public Mono<DbCompetitionRoundInfo> submitRoundForScheduler(DbCompetition sourceCompetition, DbCompetitionRoundInfo roundInfo) {
        var key = getKey(roundInfo);

        long delay = key.getStartTime() + sourceCompetition.getParameters().getRoundLengthInSeconds()
                        + roundInfo.getAdditionalMinutes() * 60
                        - LocalDateTime.now().atOffset(ZoneOffset.UTC).toEpochSecond();//ATTENTION

        return processors.compute(key, (__, before) -> {
            if (Objects.nonNull(before)) {
                before.cancel();
            }

            var res = Mono.just(roundInfo).delayElement(Duration.ofSeconds(delay)).toProcessor();
            //DBG
            res.subscribe((el) -> {
                System.out.println(el);
            });
            return res;
        });
    }

    @Override
    public void removeRoundFromScheduler(DbCompetition sourceCompetition, DbCompetitionRoundInfo roundInfo) {
        var key = getKey(roundInfo);

        processors.compute(key, (__, before) -> {
            if (Objects.nonNull(before)) {
                before.cancel();
            }
            return null;
        });
    }

    private CompetitionRoundInfoSchedulerDto getKey(DbCompetitionRoundInfo roundInfo) {
        return CompetitionRoundInfoSchedulerDto
                .builder()
                .startTime(roundInfo.getStartTime().atOffset(ZoneOffset.UTC).toEpochSecond())
                .additionalMinutes(roundInfo.getAdditionalMinutes())
                .id(roundInfo.getId())
                .build();
    }
}
