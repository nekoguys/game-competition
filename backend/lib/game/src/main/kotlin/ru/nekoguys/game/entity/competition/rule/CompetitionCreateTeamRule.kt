package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.repository.CompetitionPropertiesRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository

sealed class CompetitionCreateTeamMessage : CompetitionMessage() {
    data class TeamCreated(
        val id: Long,
        val name: String,
    ) : CompetitionCreateTeamMessage()
}

@Component
class CompetitionCreateTeamRule(
    private val competitionPropertiesRepository: CompetitionPropertiesRepository,
    private val competitionTeamRepository: CompetitionTeamRepository,
) : CompetitionRule<CompetitionPlayer.Unknown, CompetitionCommand.CreateTeam, CompetitionCreateTeamMessage> {

    override suspend fun process(
        player: CompetitionPlayer.Unknown,
        command: CompetitionCommand.CreateTeam,
    ): List<CompGameMessage<CompetitionCreateTeamMessage>> {
        val properties =
            competitionPropertiesRepository.loadBySessionId(player.sessionId)

        competitionTeamRepository.create(
            creator = player,
            name = command.teamName,
            maxTeams = properties.settings.maxTeamsAmount,
        )

        return emptyList()
    }
}
