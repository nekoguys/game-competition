package ru.nekoguys.game.entity.competition.rule

import org.springframework.stereotype.Component
import ru.nekoguys.game.core.util.buildResponse
import ru.nekoguys.game.entity.competition.model.CompetitionBasePlayer
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.InternalPlayer
import ru.nekoguys.game.entity.competition.repository.CompetitionSessionRepository
import ru.nekoguys.game.entity.competition.repository.load

data class CompetitionAnnouncementMessage(
    val announcement: String,
) : CompetitionMessage()

@Component
class SendAnnouncement(
    private val competitionSessionRepository: CompetitionSessionRepository,
) : CompetitionRule<InternalPlayer, CompetitionCommand.SendAnnouncement, CompetitionMessage> {

    override suspend fun process(
        player: InternalPlayer,
        command: CompetitionCommand.SendAnnouncement,
    ): List<CompGameMessage<CompetitionMessage>> {
        val session = competitionSessionRepository
            .load(
                id = player.sessionId,
                CompetitionSession.WithTeamIds
            )

        val message = CompetitionAnnouncementMessage(
            announcement = command.announcement,
        )

        return buildResponse {
            session.teamIds {
                +message
            }
        }
    }

    suspend fun sendAnnouncement(
        player: CompetitionBasePlayer,
        command: CompetitionCommand.SendAnnouncement,
    ): List<CompGameMessage<CompetitionMessage>> {
        require(player is InternalPlayer) { "Player $player must be internal" }
        return process(player, command)
    }
}
