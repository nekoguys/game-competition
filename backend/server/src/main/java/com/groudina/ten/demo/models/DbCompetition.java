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
    @Builder
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

        private String instruction;

        @Field("show_prev_results")
        private boolean shouldShowStudentPreviousRoundResults;

        @Field("should_force_end")
        private boolean shouldEndRoundBeforeAllAnswered;

        @Field("show_result_table")
        private boolean shouldShowResultTableInEnd;
    }
}
