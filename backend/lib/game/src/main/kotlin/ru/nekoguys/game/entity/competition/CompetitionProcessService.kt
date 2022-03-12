package ru.nekoguys.game.entity.competition

import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.session.GameSession
import ru.nekoguys.game.core.session.accept
import ru.nekoguys.game.core.session.createGameSession
import ru.nekoguys.game.core.session.getAllMessages
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.commongame.service.GameMessageLogProvider
import ru.nekoguys.game.entity.commongame.service.createGameLog
import ru.nekoguys.game.entity.competition.model.CompetitionBasePlayer
import ru.nekoguys.game.entity.competition.model.CompetitionPlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.model.InternalPlayer
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
    private val gameMessageLogProvider: GameMessageLogProvider,
) {
    private val launchedSessions =
        ConcurrentHashMap<CommonSession.Id, CompetitionLaunchedSession>()

    private val sessionsContext = SupervisorJob()

    suspend fun acceptCommand(
        sessionId: CommonSession.Id,
        user: User,
        command: CompetitionCommand,
    ) {
        acceptCommand(
            player = competitionPlayerRepository.load(sessionId, user),
            command = command,
        )
    }

    suspend fun acceptInternalCommand(
        sessionId: CommonSession.Id,
        command: CompetitionCommand,
    ) {
        acceptCommand(
            player = InternalPlayer(sessionId),
            command = command,
        )
    }

    private suspend fun acceptCommand(
        player: CompetitionBasePlayer,
        command: CompetitionCommand,
    ) {
        launchedSessions
            .getOrPut(player.sessionId) { launchGameSession(player.sessionId) }
            .accept(player, command)
    }

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
            parentContext = sessionsContext,
            messageLog = gameMessageLogProvider.createGameLog(sessionId),
            onClose = { launchedSessions.remove(sessionId) }
        )
}
