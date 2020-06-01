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
import java.util.Objects;

@Document("team")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DbTeam {
    @Id
    @Getter
    private String id;

    @Getter
    private String name;

    @Getter
    private int idInGame;//should be generated sequentially

    @Getter
    private String password;

    @DBRef
    @Getter
    private DbCompetition sourceCompetition;

    @DBRef
    @Getter
    private DbUser captain;

    @DBRef
    @Builder.Default
    private List<DbUser> allPlayers = new ArrayList<>();

    public void addPlayer(DbUser newPlayer) {
        allPlayers.add(newPlayer);
    }

    public List<DbUser> getAllPlayers() {
        if (captain != null) {
            var players = new ArrayList<>(allPlayers);
            players.add(captain);

            return players;
        }

        return allPlayers;
    }

    public int getTeamSize() {
        return allPlayers.size() + (Objects.isNull(captain) ? 1 : 0);
    }
}
