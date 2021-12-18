package ru.selemilka.game.rps

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.Game
import ru.selemilka.game.core.base.StoppedGameException
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSession
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.rules.RpsResponse
import ru.selemilka.game.rps.rules.RpsRootCommand
import ru.selemilka.game.rps.rules.RpsRootMessage
import ru.selemilka.game.rps.rules.RpsRootRule
import ru.selemilka.game.rps.storage.RpsPlayerStorage
import ru.selemilka.game.rps.storage.RpsSessionStorage
import java.util.concurrent.ConcurrentHashMap

@Configuration
@ComponentScan
class RpsGameConfiguration {
}

@Service
class RpsGameService(
    private val rpsRootRule: RpsRootRule,
    private val sessionStorage: RpsSessionStorage,
    private val playerStorage: RpsPlayerStorage,
) {
    private data class LaunchedGame(
        val job: Job,
        val scope: CoroutineScope,
        val game: Game<RpsPlayer, RpsRootCommand, RpsRootMessage>,
    )

    private val launchedGames = ConcurrentHashMap<RpsSession.Id, LaunchedGame>()

    suspend fun accept(player: RpsPlayer, command: RpsRootCommand): Unit =
        launchedGames
            .getValue(player.sessionId)
            .let { (_, scope, game) ->
                withContext(scope.coroutineContext) {
                    game.accept(player, command)
                }
            }


    fun getAllMessages(sessionId: RpsSession.Id): Flow<RpsResponse<RpsRootMessage>> =
        launchedGames
            .getValue(sessionId)
            .game
            .getAllMessages()

    fun getMessages(player: RpsPlayer): Flow<RpsRootMessage> =
        launchedGames
            .getValue(player.sessionId)
            .game
            .getMessages(player)

    suspend fun start(sessionSettings: RpsSessionSettings): RpsSession.Id {
        val job = SupervisorJob()
        val scope = CoroutineScope(Dispatchers.Default + job)
        val game = with(scope) { Game(rpsRootRule::process) }

        val sessionId = sessionStorage.createSession(sessionSettings)
        launchedGames[sessionId] = LaunchedGame(job, scope, game)

        return sessionId
    }

    suspend fun stop(sessionId: RpsSession.Id): Unit =
        launchedGames
            .getValue(sessionId)
            .let { (job, scope, game) ->
                try {
                    withContext(scope.coroutineContext) { game.stop() }
                    job.cancel()
                } catch (_: StoppedGameException) {
                    // ignore
                } finally {
                    job.cancel()
                }
            }
}
