package com.groudina.ten.demo.services;

import com.groudina.ten.demo.datasource.DbCompetitionsRepository;
import com.groudina.ten.demo.dto.TeamCreationEventDto;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbTeam;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.ReplayProcessor;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TeamConnectionNotifyServiceImpl implements ITeamConnectionNotifyService {

    private IEntitiesMapper<DbTeam, TeamCreationEventDto> mapper;

    private DbCompetitionsRepository repository;

    private Map<String, Flux<TeamCreationEventDto>> storage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<TeamCreationEventDto>> sink = new ConcurrentHashMap<>();

    public TeamConnectionNotifyServiceImpl(
            @Autowired IEntitiesMapper<DbTeam, TeamCreationEventDto> mapper,
            @Autowired DbCompetitionsRepository repository) {
        this.mapper = mapper;
        this.repository = repository;
    }

    private Flux<TeamCreationEventDto> createReplayProcessor(DbCompetition competition, String pin) {
        var processor = ReplayProcessor.<TeamCreationEventDto>create().serialize();

        sink.computeIfAbsent(pin, (key) -> {
            var sink = processor.sink();

            competition.getTeams().forEach((teamFromSourceCompetition) -> {
                sink.next(mapper.map(teamFromSourceCompetition, null));
            });

            return sink;
        });

        return processor;
    }

    @Override
    public Flux<TeamCreationEventDto> getTeamEventForGame(String pin) {

        return storage.computeIfAbsent(pin, (__) -> {
            var processor = ReplayProcessor.<TeamCreationEventDto>create().serialize();

            sink.computeIfAbsent(pin, (key) -> {
                var sink = processor.sink();

                repository.findByPin(pin).subscribe((val) -> {
                    log.debug("Mapping teams");
                    val.getTeams().forEach(team -> {
                        sink.next(mapper.map(team, null));
                    });
                });
                return sink;
            });

            return processor;
        });
    }

    @Override
    public void registerTeam(DbTeam team) {
        var sourceCompetition = team.getSourceCompetition();

        storage.compute(sourceCompetition.getPin(), (pin, before) -> {
            if (Objects.isNull(before)) {
                return createReplayProcessor(sourceCompetition, pin);
            } else {
                var event = mapper.map(team, null);
                sink.get(pin).next(event);
                return before;
            }
        });
    }


}
