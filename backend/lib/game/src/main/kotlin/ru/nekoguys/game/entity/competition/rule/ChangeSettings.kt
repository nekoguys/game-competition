package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.entity.competition.model.CompetitionBasePlayer
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionSettingsRepository
import ru.nekoguys.game.entity.competition.repository.load

@Component
class CompetitionChangeSettingsRule(
    private val competitionSettingsRepository: CompetitionSettingsRepository,
    private val competitionSessionRepository: CompetitionSessionRepository,
) : CompetitionRule<CompetitionPlayer.Teacher, CompetitionCommand.ChangeCompetitionSettings, Nothing> {

    override suspend fun process(
        player: CompetitionPlayer.Teacher,
        command: CompetitionCommand.ChangeCompetitionSettings,
    ): List<GameMessage<CompetitionTeam.Id, Nothing>> {

        val competition = competitionSessionRepository.load(
            id = player.sessionId,
            CompetitionSession.WithCommonFields
        )
        require(player.user.id == competition.creatorId)

        competitionSettingsRepository
            .update(player.sessionId, command.newSettings)
        return emptyList()
    }
}

suspend fun CompetitionChangeSettingsRule.changeSettings(
    player: CompetitionBasePlayer,
    command: CompetitionCommand.ChangeCompetitionSettings,
): List<CompGameMessage<Nothing>> {
    require(player is CompetitionPlayer.Teacher) { "Player $player must be teacher" }
    return process(player, command)
}
