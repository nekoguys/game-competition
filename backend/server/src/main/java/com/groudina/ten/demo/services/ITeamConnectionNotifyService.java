package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.TeamCreationEventDto;
import com.groudina.ten.demo.models.DbTeam;
import reactor.core.publisher.Flux;

public interface ITeamConnectionNotifyService {
    Flux<TeamCreationEventDto> getTeamEventForGame(String pin);

    void registerTeam(DbTeam team);
}
