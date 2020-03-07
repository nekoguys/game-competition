package com.groudina.ten.demo.services;

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

        private Map<Integer, Double> prices;

        private Map<Integer, ? extends Map<Integer, Double>> income;

        private Map<Integer, ? extends Map<Integer, Integer>> produced;

        private Map<Integer, ? extends List<String>> teamMembers;

        private List<Integer> teamsOrderInDecreasingByTotalPrice;

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Map<Integer, Double> prices;

            private Map<Integer, ? extends Map<Integer, Double>> income;

            private Map<Integer, ? extends Map<Integer, Integer>> produced;

            private Map<Integer, ? extends List<String>> teamMembers;

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

            public Builder setTeamMembers(Map<Integer, ? extends List<String>> teamMembers) {
                this.teamMembers = teamMembers;
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

                return new CompetitionResults(prices, income, produced, teamMembers, teamsOrder);
            }
        }
    }

    CompetitionResults getCompetitionResults(DbCompetition competition);
}
