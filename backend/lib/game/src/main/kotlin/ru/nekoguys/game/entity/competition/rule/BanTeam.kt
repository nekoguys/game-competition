package ru.nekoguys.game.entity.competition.rule

import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.core.util.defer
import ru.nekoguys.game.entity.competition.model.CompetitionBasePlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.model.InternalPlayer
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionTeamRepository

data class CompetitionTeamBannedMessage(
    val teamId: CompetitionTeam.Id,
    val teamNumber: Int,
    val teamName: String,
    val roundNumber: Int,
    val reason: String,
) : CompetitionMessage()

@Component
class CompetitionBanTeamRule(
    private val competitionTeamRepository: CompetitionTeamRepository,
    private val competitionRoundRepository: CompetitionRoundRepository,
) : CompetitionRule<InternalPlayer, CompetitionCommand.BanTeams, CompetitionTeamBannedMessage> {

    override suspend fun process(
        player: InternalPlayer,
        command: CompetitionCommand.BanTeams,
    ): List<GameMessage<CompetitionTeam.Id, CompetitionTeamBannedMessage>> {
        val teams = competitionTeamRepository
            .findAllByIds(command.teamIds.map { it.number })
            .toList()

        val roundNumber = competitionRoundRepository
            .findAll(listOf(player.sessionId.number))
            .maxOf { it.roundNumber }

        for (team in teams) {
            competitionTeamRepository.update(
                from = team,
                to = team.copy(
                    banRoundNumber = roundNumber,
                )
            )
        }

        return buildResponse {
            for (team in teams) {
                team.id {
                    +CompetitionTeamBannedMessage(
                        teamId = team.id,
                        teamNumber = team.numberInGame,
                        teamName = team.name,
                        roundNumber = roundNumber,
                        reason = command.reason,
                    )
                }
                defer(
                    fromPlayer = InternalPlayer(sessionId = player.sessionId),
                    command = CompetitionCommand.SendAnnouncement(
                        announcement = " Game: Team ${team.numberInGame}: \"${team.name}\"" +
                                " is banned for exceeding loss limit"
                    )
                )
            }
        }
    }
}

suspend fun CompetitionBanTeamRule.banTeam(
    player: CompetitionBasePlayer,
    command: CompetitionCommand.BanTeams,
): List<GameMessage<CompetitionTeam.Id, CompetitionTeamBannedMessage>> {
    if (player !is InternalPlayer) {
        error("Player $player must be internal")
    }
    return process(player, command)
}
