package ru.nekoguys.game.entity.competition.service

import org.springframework.stereotype.Service
import ru.nekoguys.game.entity.competition.model.CompetitionRoundAnswer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam

@Service
class RoundResultsCalculator {
    fun calculateResults(
        roundAnswers: List<CompetitionRoundAnswer>,
    ): RoundResults {
        return RoundResults(
            results = roundAnswers
                .map { it.teamId }
                .associateWith { 0.0 },
            bannedTeamIds = listOf(roundAnswers.random().teamId),
            price = 42.0,
        )
    }
}

data class RoundResults(
    val results: Map<CompetitionTeam.Id, Double>,
    val bannedTeamIds: List<CompetitionTeam.Id>,
    val price: Double,
)
