package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.TeamCreationEventDto;
import com.groudina.ten.demo.models.DbTeam;
import com.groudina.ten.demo.models.DbUser;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class DbTeamToTeamCreationEvent implements IEntitiesMapper<DbTeam, TeamCreationEventDto> {

    @Override
    public TeamCreationEventDto map(DbTeam from, Iterable<Pair<String, ?>> additionalFields) {
        return TeamCreationEventDto.builder()
                .idInGame(from.getIdInGame())
                .teamMembers(from.getAllPlayers().stream().map(DbUser::getEmail)
                        .collect(Collectors.toList()))
                .teamName(from.getName())
                .build();
    }
}
