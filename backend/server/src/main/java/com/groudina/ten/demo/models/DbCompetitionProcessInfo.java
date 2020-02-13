package com.groudina.ten.demo.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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

    @Builder.Default
    private int currentRoundNumber = 0;

    public void addRoundInfo(DbCompetitionRoundInfo roundInfo) {
        roundInfos.add(roundInfo);
        currentRoundNumber++;
    }

    public DbCompetitionRoundInfo getCurrentRound() {
        return roundInfos.get(currentRoundNumber - 1);
    }
}
