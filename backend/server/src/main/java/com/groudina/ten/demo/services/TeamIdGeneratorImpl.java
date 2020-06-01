package com.groudina.ten.demo.services;

import com.groudina.ten.demo.models.DbCompetition;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class TeamIdGeneratorImpl implements ITeamIdGenerator {

    private static ConcurrentHashMap<String, AtomicInteger> teamsId = new ConcurrentHashMap<>();

    @Override
    public int generate(DbCompetition competition) {
        String pin = competition.getPin();
        teamsId.putIfAbsent(pin, new AtomicInteger(competition.getTeams().size()));
        return teamsId.get(pin).incrementAndGet();
    }
}
