package com.groudina.ten.demo.services;

import com.groudina.ten.demo.dto.CompetitionMessageDto;
import com.groudina.ten.demo.dto.TeamCreationEventDto;
import com.groudina.ten.demo.models.DbCompetition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public interface ICompetitionResultsFormatter {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CompetitionResults implements Serializable {
        private static final long serialVersionUID = -5849180842391836493L;

        private String competitionName;

        private String instruction;

        private Map<Integer, Double> prices;

        private Map<Integer, ? extends Map<Integer, Double>> income;

        private Map<Integer, ? extends Map<Integer, Integer>> produced;

        private List<TeamCreationEventDto> teams;

        private List<Integer> teamsOrderInDecreasingByTotalPrice;

        private List<CompetitionMessageDto> messages;

        private Map<Integer, IStrategySubmissionService.StrategyHolder> strategyHolders;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String competitionName;

            private String instruction;

            private Map<Integer, Double> prices;

            private Map<Integer, ? extends Map<Integer, Double>> income;

            private Map<Integer, ? extends Map<Integer, Integer>> produced;

            private List<TeamCreationEventDto> teams;

            private List<CompetitionMessageDto> messages;

            private Map<Integer, IStrategySubmissionService.StrategyHolder> strategyHolders;

            public Builder setCompetitionName(String competitionName) {
                this.competitionName = competitionName;
                return this;
            }

            public Builder setPrices(Map<Integer, Double> prices) {
                this.prices = prices;
                return this;
            }

            public Builder setIncome(Map<Integer, ? extends Map<Integer, Double>> income) {
                this.income = income;
                return this;
            }

            public Builder setProduced(Map<Integer, ? extends Map<Integer, Integer>> produced) {
                this.produced = produced;
                return this;
            }

            public Builder setMessage(List<CompetitionMessageDto> messages) {
                this.messages = messages;
                return this;
            }

            public Builder setTeams(List<TeamCreationEventDto> teams) {
                this.teams = teams;
                return this;
            }

            public Builder instruction(String instruction) {
                this.instruction = instruction;
                return this;
            }

            public Builder strategyHolders(Map<Integer, IStrategySubmissionService.StrategyHolder> lst) {
                this.strategyHolders = lst;
                return this;
            }

            public CompetitionResults build() {
                Map<Integer, Double> totalIncome = new HashMap<>();
                income.entrySet().forEach(entrySet -> {
                    entrySet.getValue().entrySet().stream().forEach(el -> {
                        totalIncome.compute(el.getKey(), (key, before) -> {
                            if (Objects.isNull(before)) {
                                return el.getValue();
                            }
                            return before + el.getValue();
                        });
                    });
                });

                List<Integer> teamsOrder = totalIncome.entrySet().stream()
                        .sorted(Comparator.<Map.Entry<Integer, Double>>comparingDouble(Map.Entry::getValue).reversed())
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toList());

                return new CompetitionResults(competitionName, instruction, prices, income, produced, teams, teamsOrder,
                        messages, strategyHolders);
            }
        }
    }

    CompetitionResults getCompetitionResults(DbCompetition competition);
}
