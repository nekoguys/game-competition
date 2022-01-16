package ru.selemilka.game.rps

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.*
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSession
import ru.selemilka.game.rps.model.RpsSessionSettings
import ru.selemilka.game.rps.rule.RpsCommand
import ru.selemilka.game.rps.rule.RpsMessage
import ru.selemilka.game.rps.rule.RpsRootRule
import ru.selemilka.game.rps.storage.RpsMessageLog
import ru.selemilka.game.rps.storage.RpsSessionStorage
import java.util.concurrent.ConcurrentHashMap

@Configuration
@ComponentScan
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
typealias RpsGameSession = GameSession<GameCommandRequest<RpsPlayer, RpsCommand>, RpsGameMessage<RpsMessage>>

data class RpsLaunchedSession(
    val id: RpsSession.Id,
    val session: RpsGameSession,
    val writeToLogJob: Job,
)

/**
 * Сервис для создания, завершения игровых сессий.
 */
@Service
class RpsGameService(
    private val rpsRootRule: RpsRootRule,
    private val sessionStorage: RpsSessionStorage,
    private val messageStorage: RpsMessageLog,
) {
    private val launchedSessions = ConcurrentHashMap<RpsSession.Id, RpsLaunchedSession>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    suspend fun startSession(
        settings: RpsSessionSettings = RpsSessionSettings(),
    ): RpsLaunchedSession {
        val id = sessionStorage.createSession(settings)

        val session = coroutineScope.launchGameSession(
            rule = rpsRootRule,
            onClose = {
                val removedSession = launchedSessions.remove(id)
                removedSession?.writeToLogJob?.join()
            },
            replay = 1,
        )

        val loggedSession = LoggedRpsGameSession(
            id = id,
            innerSession = session,
            messageStorage = messageStorage,
        )

        val writeToLogJob = coroutineScope.launch {
            messageStorage.save(
                sessionId = id,
                messages = session.getAllMessages()
            )
        }

        return RpsLaunchedSession(id, loggedSession, writeToLogJob)
            .also {
                launchedSessions[id] = it
            }
    }
}

private class LoggedRpsGameSession(
    val id: RpsSession.Id,
    val innerSession: RpsGameSession,
    val messageStorage: RpsMessageLog,
) : RpsGameSession {

    override suspend fun accept(request: GameCommandRequest<RpsPlayer, RpsCommand>) {
        innerSession.accept(request)
    }

    override fun getAllMessagesIndexed(): Flow<IndexedValue<RpsGameMessage<RpsMessage>>> =
        flow {
            var lastSentMessageIndex = -1

            messageStorage
                .load(id)
                .collect { message ->
                    emit(IndexedValue(++lastSentMessageIndex, message))
                }

            innerSession
                .getAllMessagesIndexed()
                .filter { (index, _) -> index > lastSentMessageIndex }
                .collect { indexedMessage ->
                    check(indexedMessage.index == ++lastSentMessageIndex) {
                        "Some messages are lost!"
                    }

                    emit(indexedMessage)
                }
        }
}
