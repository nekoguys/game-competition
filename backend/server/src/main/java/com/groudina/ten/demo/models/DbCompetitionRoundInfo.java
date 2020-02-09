package com.groudina.ten.demo.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comp_round_info")
public class DbCompetitionRoundInfo {
    private LocalDateTime startTime;
    private int additionalMinutes;

    @DBRef
    @Builder.Default
    private List<DbRoundResultElement> roundResult = new ArrayList<>();

    @DBRef
    @Builder.Default
    private List<DbAnswer> answerList = new ArrayList<>();

    public void addAnswer(DbAnswer answer) {
        this.answerList.add(answer);
    }

    public void addOneMinute() {
        this.additionalMinutes += 1;
    }

    public void addRoundResult(DbRoundResultElement element) {
        this.roundResult.add(element);
    }
}
