package ru.selemilka.game.rps

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.GameCommandRequest
import ru.selemilka.game.core.base.GameMessage
import ru.selemilka.game.core.base.GameRule
import ru.selemilka.game.core.session.GameSession
import ru.selemilka.game.core.session.launchGameSession
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSession
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.rule.RpsCommand
import ru.selemilka.game.rps.rule.RpsMessage
import ru.selemilka.game.rps.rule.RpsRootRule
import ru.selemilka.game.rps.storage.RpsSessionStorage
import ru.selemilka.game.rps.util.RpsCommandLogProvider
import ru.selemilka.game.rps.util.RpsMessageLogProvider
import ru.selemilka.game.rps.util.TraceIdProviderFactory
import java.util.concurrent.ConcurrentHashMap

@Configuration
@ComponentScan
@EnableConfigurationProperties
class RpsGameConfiguration

/**
 * Сообщение Msg, отправленное игроку типа [RpsPlayer.Human]
 */
typealias RpsGameMessage<Msg> = GameMessage<RpsPlayer.Human, Msg>

/**
 * Игровое правило, которое для команды Cmd игрока P
 * возвращает сообщения Msg игрокам RpsPlayer.Human
 */
typealias RpsGameRule<P, Cmd, Msg> = GameRule<P, Cmd, RpsGameMessage<Msg>>

/**
 * Тип игровой сессии в этой игрушечной "Камень-ножницы-бумаге"
 */
class RpsGameSession(
    val id: RpsSession.Id,
    coreSession: GameSession<GameCommandRequest<RpsPlayer, RpsCommand>, RpsGameMessage<RpsMessage>>,
) : GameSession<GameCommandRequest<RpsPlayer, RpsCommand>, RpsGameMessage<RpsMessage>> by coreSession

/**
 * Сервис для создания, завершения игровых сессий.
 */
@Service
class RpsGameService(
    private val rpsRootRule: RpsRootRule,
    private val sessionStorage: RpsSessionStorage,
    private val messageLogProvider: RpsMessageLogProvider,
    private val commandLogProvider: RpsCommandLogProvider,
    private val traceIdProviderFactory: TraceIdProviderFactory,
) {
    private val launchedSessions = ConcurrentHashMap<RpsSession.Id, RpsGameSession>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    suspend fun startSession(
        settings: RpsSessionSettings = RpsSessionSettings(),
    ): RpsGameSession {
        val sessionId = sessionStorage.createSession(settings)

        val coreSession = coroutineScope.launchGameSession(
            rule = rpsRootRule,
            onClose = { launchedSessions.remove(sessionId) },
            messageLog = messageLogProvider.getMessageLog(sessionId),
            commandLog = commandLogProvider.getCommandLog(sessionId),
            traceIdProvider = traceIdProviderFactory.getProvider(sessionId),
        )

        return RpsGameSession(
            id = sessionId,
            coreSession = coreSession
        ).also { launchedSessions[sessionId] = it }
    }
}
