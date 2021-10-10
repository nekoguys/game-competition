package com.groudina.ten.demo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Document(collection = "comp_process_info")
public class DbCompetitionProcessInfo {
    @Id
    private String id;

    @DBRef
    @Builder.Default
    private List<DbCompetitionRoundInfo> roundInfos = new ArrayList<>();

    @DBRef
    @Builder.Default
    private List<DbCompetitionMessage> messages = new ArrayList<>();

    @Builder.Default
    @Setter
    private int currentRoundNumber = 0;

    public void addRoundInfo(DbCompetitionRoundInfo roundInfo) {
        roundInfos.add(roundInfo);
        currentRoundNumber++;
    }

    public void addMessage(DbCompetitionMessage message) {
        this.messages.add(message);
    }

    public DbCompetitionRoundInfo getCurrentRound() {
        return roundInfos.get(currentRoundNumber - 1);
    }
}
