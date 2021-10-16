package com.groudina.ten.demo.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

import java.util.*;

public class GameRoundDataStorage {
    private Map<String, RoundDataStorageMock> gameToRoundStorage = new HashMap<>();

    public static class RoundDataStorageMock {
        @AllArgsConstructor
        @Data
        public static class Answer {
            private int value;

            @NonNull
            private Team team;
        }

        @Data
        @EqualsAndHashCode
        public static class Team {
            @NonNull
            private String id;
        }

        private List<Answer> answers = new ArrayList<>();

        public RoundDataStorageMock() {
            this(new ArrayList<>());
        }

        public RoundDataStorageMock(
                List<Answer> answers
        ) {
            this.answers = answers;
        }

        public void addAnswer(Answer answer) {
            var previousAnswerCandidate = answers.stream()
                    .filter(answer1 -> answer.team.equals(answer1.team))
                    .findAny();
            previousAnswerCandidate.ifPresent(el -> answers.remove(el));

            answers.add(answer);
        }

        public List<Answer> getAnswers() {
            return new ArrayList<>(answers);
        }

        public void clear() {
            answers.clear();
        }
    }

    public void addAnswer(String gamePin, RoundDataStorageMock.Answer answer) {
        gameToRoundStorage.putIfAbsent(gamePin, new RoundDataStorageMock());
        gameToRoundStorage.computeIfPresent(gamePin, (pin, storage) -> {
            storage.addAnswer(answer);
            return storage;
        });
    }

    public Optional<RoundDataStorageMock> getStorageByPin(String gamePin) {
        return Optional.ofNullable(gameToRoundStorage.get(gamePin));
    }

    public void flushRound(String gamePin) {
        Optional.ofNullable(gameToRoundStorage.get(gamePin)).ifPresent(RoundDataStorageMock::clear);
    }
}
