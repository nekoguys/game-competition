package ru.selemilka.game.rps

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.GameSession
import ru.selemilka.game.core.base.launchGameSession
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSession
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.rule.RpsCommand
import ru.selemilka.game.rps.rule.RpsMessage
import ru.selemilka.game.rps.rule.RpsRootRule
import ru.selemilka.game.rps.storage.RpsSessionStorage
import java.util.concurrent.ConcurrentHashMap

@Configuration
@ComponentScan
class RpsGameConfiguration

data class RpsLaunchedSession(
    val id: RpsSession.Id,
    val session: GameSession<RpsPlayer, RpsCommand, RpsMessage>,
)

/**
 * Сервис для создания, завершения игровых сессий.
 */
@Service
class RpsGameService(
    private val rpsRootRule: RpsRootRule,
    private val sessionStorage: RpsSessionStorage,
) {
    private val launchedSessions = ConcurrentHashMap<RpsSession.Id, RpsLaunchedSession>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    suspend fun startSession(
        settings: RpsSessionSettings = RpsSessionSettings(),
    ): RpsLaunchedSession {
        val id = sessionStorage.createSession(settings)
        val launchedSession = RpsLaunchedSession(
            id,
            session = coroutineScope.launchGameSession(
                rule = rpsRootRule,
                onClose = { launchedSessions.remove(id) }
            ),
        )
        launchedSessions[id] = launchedSession
        return launchedSession
    }
}
