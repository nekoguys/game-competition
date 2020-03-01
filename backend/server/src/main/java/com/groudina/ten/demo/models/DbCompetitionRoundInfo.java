package com.groudina.ten.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "comp_round_info")
public class DbCompetitionRoundInfo {

    @Id
    private String id;

    private LocalDateTime startTime;
    private int additionalMinutes;

    @Setter
    private boolean isEnded;

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

    public void addRoundResults(Collection<DbRoundResultElement> elements) {
        this.roundResult.addAll(elements);
    }


}
