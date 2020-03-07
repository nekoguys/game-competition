package com.groudina.ten.demo.services;


import com.groudina.ten.demo.dto.CompetitionMessageDto;
import com.groudina.ten.demo.dto.TeamCreationEventDto;
import com.groudina.ten.demo.models.DbCompetition;
import com.groudina.ten.demo.models.DbUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class CompetitionResultsFormatterImpl implements ICompetitionResultsFormatter {

    @AllArgsConstructor
    @Getter
    private static class TeamDto {
        private int teamIdInGame;
        private List<String> teamMembers;
    }

    @Override
    public CompetitionResults getCompetitionResults(DbCompetition competition) {
        Map<Integer, Double> prices = new HashMap<>();
        Map<Integer, HashMap<Integer, Double>> income = new HashMap<>();
        Map<Integer, HashMap<Integer, Integer>> produce = new HashMap<>();
        List<TeamCreationEventDto> teamMembers = new ArrayList<>();
        List<CompetitionMessageDto> messages;

        messages = competition.getCompetitionProcessInfo().getMessages().stream().map(el ->
                CompetitionMessageDto
                .builder()
                .message(el.getMessage())
                .sendTime(el.getSendTime().atOffset(ZoneOffset.UTC).toEpochSecond())
                .build()
        ).collect(Collectors.toList());

        for (int i = 0; i < competition.getCompetitionProcessInfo().getRoundInfos().size(); ++i) {
            var currRoundInfo = competition.getCompetitionProcessInfo().getRoundInfos().get(i);

            prices.put(i + 1, currRoundInfo.getPrice());

            for (var roundResult : currRoundInfo.getRoundResult()) {
                income.compute(i + 1, (key, before) -> {
                    if (Objects.isNull(before)) {
                        HashMap<Integer, Double> mp = new HashMap<>();
                        mp.put(roundResult.getTeam().getIdInGame(), roundResult.getIncome());
                        return mp;
                    } else {
                        before.put(roundResult.getTeam().getIdInGame(), roundResult.getIncome());
                        return before;
                    }
                });
            }

            for (var answer : currRoundInfo.getAnswerList()) {
                produce.compute(i + 1, (key, before) -> {
                    if (Objects.isNull(before)) {
                        HashMap<Integer, Integer> mp = new HashMap<>();
                        mp.put(answer.getSubmitter().getIdInGame(), answer.getValue());
                        return mp;
                    } else {
                        before.put(answer.getSubmitter().getIdInGame(), answer.getValue());

                        return before;
                    }
                });
            }
        }

        competition.getTeams().forEach(el -> {
            teamMembers.add(
                    TeamCreationEventDto.builder()
                            .teamName(el.getName())
                            .teamMembers(el.getAllPlayers().stream().map(DbUser::getEmail).collect(Collectors.toList()))
                            .idInGame(el.getIdInGame())
                            .build());
        });

        teamMembers.sort(Comparator.comparingInt(TeamCreationEventDto::getIdInGame));
        

        return ICompetitionResultsFormatter.CompetitionResults.builder()
                .setCompetitionName(competition.getParameters().getName())
                .setIncome(income)
                .setPrices(prices)
                .setProduced(produce)
                .setMessage(messages)
                .setTeams(teamMembers)
                .build();
    }
}
