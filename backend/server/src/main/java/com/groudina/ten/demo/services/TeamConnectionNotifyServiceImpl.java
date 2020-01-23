package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.TeamCreationEventDto;
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

    private Map<String, Flux<TeamCreationEventDto>> storage = new ConcurrentHashMap<>();
    private Map<String, FluxSink<TeamCreationEventDto>> sink = new ConcurrentHashMap<>();

    public TeamConnectionNotifyServiceImpl(@Autowired IEntitiesMapper<DbTeam, TeamCreationEventDto> mapper) {
        this.mapper = mapper;
    }

    @Override
    public Flux<TeamCreationEventDto> getTeamEventForGame(String pin) {
        var result = storage.get(pin);
        if (result == null) {
            log.warn("Tried to subscribe to it existent team creation events, pin: " + pin);
            return Flux.empty();
        }
        return storage.get(pin);
    }

    @Override
    public void registerTeam(DbTeam team) {
        var sourceCompetition = team.getSourceCompetition();

        storage.compute(sourceCompetition.getPin(), (pin, before) -> {
            if (Objects.isNull(before)) {
                var processor = ReplayProcessor.<TeamCreationEventDto>create().serialize();

                sink.computeIfAbsent(pin, (key) -> {
                    var sink = processor.sink();
                    sink.next(mapper.map(team, null));

                    return sink;
                });

                return processor;
            } else {
                var event = mapper.map(team, null);
                sink.get(pin).next(event);
                return before;
            }
        });
    }


}
