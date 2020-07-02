package com.groudina.ten.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
@Document(collection="competition")
public class DbCompetition {
    @Id
    private String id;

    private DbCompetition.State state;

    public void setState(State newState) {
        state = newState;
        lastModifiedDate = LocalDateTime.now();
    }

    private String pin;

    private DbCompetition.Parameters parameters;

    @Builder.Default
    @DBRef
    private List<DbTeam> teams = new ArrayList<>();

    @DBRef
    private DbUser owner;

    @Setter
    @DBRef
    private DbCompetitionProcessInfo competitionProcessInfo;

    public void addTeam(DbTeam team) {
        this.teams.add(team);
    }

    public static enum State {
        Draft, Registration, InProcess, Ended
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    public static class Parameters {
        private String name;

        @Field("expenses_formula")
        private List<String> expensesFormula;//polynomial

        @Field("demand_formula")
        private List<String> demandFormula;//constant - constant*Price

        @Field("max_teams_amount")
        private int maxTeamsAmount;

        @Field("max_team_size")
        private int maxTeamSize;

        @Field("rounds_count")
        private int roundsCount;

        @Field("round_length_in_seconds")
        private int roundLengthInSeconds;

        @Field("rounds_length_history")
        private RoundsLengthHistory roundsLengthHistory = new RoundsLengthHistory();

        @Field("team_loss_upperbound")
        private double teamLossUpperbound = 10000;

        private String instruction;

        @Field("show_prev_results")
        private boolean shouldShowStudentPreviousRoundResults;

        @Field("should_force_end")
        private boolean shouldEndRoundBeforeAllAnswered;

        @Field("show_result_table")
        private boolean shouldShowResultTableInEnd;

        public static ParametersBuilder builder() {
            return new ParametersBuilder();
        }

        @AllArgsConstructor
        @NoArgsConstructor
        @Getter
        @Setter
        @Builder
        public static class RoundsLengthHistory {
            @Builder.Default
            private List<Integer> roundNumbers = new ArrayList<>();
            @Builder.Default
            private List<Integer> roundLength = new ArrayList<>();

            public void add(int round, int newRoundLength) {
                if (roundNumbers.size() > 0 && roundNumbers.get(roundNumbers.size() - 1) == round) {
                    roundLength.set(roundLength.size() - 1, newRoundLength);
                } else {
                    roundNumbers.add(round);
                    roundLength.add(newRoundLength);
                }
            }
        }

        public static class ParametersBuilder {
            private String name;

            private List<String> expensesFormula;//polynomial

            private List<String> demandFormula;//constant - constant*Price

            private int maxTeamsAmount;

            private int maxTeamSize;

            private int roundsCount;

            private int roundLengthInSeconds;

            private RoundsLengthHistory roundsLengthHistory = new RoundsLengthHistory();

            private double teamLossUpperbound = 10000;

            private String instruction;

            private boolean shouldShowStudentPreviousRoundResults;

            private boolean shouldEndRoundBeforeAllAnswered;

            private boolean shouldShowResultTableInEnd;

            public ParametersBuilder name(String name) {
                this.name = name;
                return this;
            }

            public ParametersBuilder expensesFormula(List<String> expensesFormula) {
                this.expensesFormula = expensesFormula;
                return this;
            }

            public ParametersBuilder demandFormula(List<String> demandFormula) {
                this.demandFormula = demandFormula;
                return this;
            }

            public ParametersBuilder maxTeamsAmount(int maxTeamsAmount) {
                this.maxTeamsAmount = maxTeamsAmount;
                return this;
            }

            public ParametersBuilder maxTeamSize(int maxTeamSize) {
                this.maxTeamSize = maxTeamSize;
                return this;
            }

            public ParametersBuilder roundsCount(int roundsCount) {
                this.roundsCount = roundsCount;
                return this;
            }

            public ParametersBuilder roundLengthInSeconds(int roundLengthInSeconds) {
                this.roundLengthInSeconds = roundLengthInSeconds;
                return this;
            }

            public ParametersBuilder roundsLengthHistory(RoundsLengthHistory roundsLengthHistory) {
                this.roundsLengthHistory = roundsLengthHistory;
                return this;
            }

            public ParametersBuilder teamLossUpperbound(double teamLossUpperbound) {
                this.teamLossUpperbound = teamLossUpperbound;
                return this;
            }

            public ParametersBuilder instruction(String instruction) {
                this.instruction = instruction;
                return this;
            }

            public ParametersBuilder shouldShowStudentPreviousRoundResults(boolean shouldShowStudentPreviousRoundResults) {
                this.shouldShowStudentPreviousRoundResults = shouldShowStudentPreviousRoundResults;
                return this;
            }

            public ParametersBuilder shouldEndRoundBeforeAllAnswered(boolean shouldEndRoundBeforeAllAnswered) {
                this.shouldEndRoundBeforeAllAnswered = shouldEndRoundBeforeAllAnswered;
                return this;
            }

            public ParametersBuilder shouldShowResultTableInEnd(boolean shouldShowResultTableInEnd) {
                this.shouldShowResultTableInEnd = shouldShowResultTableInEnd;
                return this;
            }

            public Parameters build() {
                if (roundsLengthHistory.getRoundNumbers().size() == 0) {
                    roundsLengthHistory.add(0, roundLengthInSeconds);
                }
                return new Parameters(name, expensesFormula, demandFormula, maxTeamsAmount, maxTeamSize, roundsCount,
                        roundLengthInSeconds, roundsLengthHistory, teamLossUpperbound, instruction,
                        shouldShowStudentPreviousRoundResults, shouldEndRoundBeforeAllAnswered, shouldShowResultTableInEnd);
            }
        }
    }

    private LocalDateTime lastModifiedDate;
}
