package ru.nekoguys.game.entity.competition

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.session.*
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionBasePlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.repository.CompetitionPlayerRepository
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionMessage
import ru.nekoguys.game.entity.competition.rule.CompetitionRootRule
import ru.nekoguys.game.entity.user.model.User
import java.util.concurrent.ConcurrentHashMap

typealias CompetitionLaunchedSession =
        GameSession<CompetitionBasePlayer, CompetitionCommand, CompetitionTeam.Id, CompetitionMessage>

@Service
class CompetitionProcessService(
    private val competitionPlayerRepository: CompetitionPlayerRepository,
    private val rootRule: CompetitionRootRule,
) {
    private val launchedSessions =
        ConcurrentHashMap<CommonSession.Id, CompetitionLaunchedSession>()

    suspend fun acceptCommand(
        sessionId: CommonSession.Id,
        user: User,
        command: CompetitionCommand,
    ) {
        launchedSessions
            .getOrPut(sessionId) { launchGameSession(sessionId) }
            .accept(
                player = competitionPlayerRepository.load(sessionId, user),
                command = command,
            )
    }

    fun getAllMessagesForTeam(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
    ): Flow<CompetitionMessage> =
        launchedSessions
            .getOrPut(sessionId) { launchGameSession(sessionId) }
            .getMessages(teamId)

    fun getAllMessagesForSession(
        sessionId: CommonSession.Id,
    ): Flow<GameMessage<CompetitionTeam.Id, CompetitionMessage>> =
        launchedSessions
            .getOrPut(sessionId) { launchGameSession(sessionId) }
            .getAllMessages()

    private fun launchGameSession(
        sessionId: CommonSession.Id,
    ): CompetitionLaunchedSession =
        createGameSession(
            rule = rootRule,
            onClose = { launchedSessions.remove(sessionId) }
        )
}
