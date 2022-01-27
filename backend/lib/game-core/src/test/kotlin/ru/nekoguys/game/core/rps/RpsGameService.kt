package ru.nekoguys.game.core.rps

import kotlinx.coroutines.SupervisorJob
import org.springframework.stereotype.Service
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.GameRule
import ru.nekoguys.game.core.rps.model.RpsPlayer
import ru.nekoguys.game.core.rps.model.RpsSession
import ru.nekoguys.game.core.rps.model.RpsSessionSettings
import ru.nekoguys.game.core.rps.rule.RpsCommand
import ru.nekoguys.game.core.rps.rule.RpsMessage
import ru.nekoguys.game.core.rps.rule.RpsRootRule
import ru.nekoguys.game.core.rps.storage.RpsSessionStorage
import ru.nekoguys.game.core.rps.util.RpsCommandLogProvider
import ru.nekoguys.game.core.rps.util.RpsMessageLogProvider
import ru.nekoguys.game.core.session.GameSession
import ru.nekoguys.game.core.session.createGameSession
import java.util.concurrent.ConcurrentHashMap

/**
 * Сообщение Msg, отправленное игроку типа [RpsPlayer.Human]
 */
typealias RpsGameMessage<Msg> = GameMessage<RpsPlayer.Human, Msg>

/**
 * Игровое правило, которое для команды Cmd игрока P
 * возвращает сообщения Msg игрокам RpsPlayer.Human
 */
typealias RpsGameRule<P, Cmd, Msg> = GameRule<P, Cmd, RpsPlayer.Human, Msg>

/**
 * Тип игровой сессии в этой игрушечной "Камень-ножницы-бумаге"
 */
class RpsGameSession(
    val id: RpsSession.Id,
    coreSession: GameSession<RpsPlayer, RpsCommand, RpsPlayer.Human, RpsMessage>,
) : GameSession<RpsPlayer, RpsCommand, RpsPlayer.Human, RpsMessage> by coreSession

/**
 * Сервис для создания, завершения игровых сессий.
 */
@Service
class RpsGameService(
    private val rpsRootRule: RpsRootRule,
    private val sessionStorage: RpsSessionStorage,
    private val messageLogProvider: RpsMessageLogProvider,
    private val commandLogProvider: RpsCommandLogProvider,
) {
    private val launchedSessions = ConcurrentHashMap<RpsSession.Id, RpsGameSession>()
    private val job = SupervisorJob()

    suspend fun startSession(
        settings: RpsSessionSettings = RpsSessionSettings(),
    ): RpsGameSession {
        val sessionId = sessionStorage.createSession(settings)

        val coreSession = createGameSession(
            rule = rpsRootRule,
            messageLog = messageLogProvider.getMessageLog(sessionId),
            commandLog = commandLogProvider.getCommandLog(sessionId),
            parentContext = job,
            onClose = { launchedSessions.remove(sessionId) },
        )

        return RpsGameSession(
            id = sessionId,
            coreSession = coreSession
        ).also { launchedSessions[sessionId] = it }
    }
}
