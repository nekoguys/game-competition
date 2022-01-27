package ru.nekoguys.game.core.rps.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import org.springframework.stereotype.Component
import ru.nekoguys.game.core.rps.model.RpsPlayer
import ru.nekoguys.game.core.rps.model.RpsSession
import ru.nekoguys.game.core.rps.rule.*
import ru.nekoguys.game.core.session.GameMessageLog
import ru.nekoguys.game.core.session.LoggedGameMessage
import java.util.*
import java.util.concurrent.ConcurrentHashMap

interface RpsMessageLog : GameMessageLog<RpsPlayer.Human, RpsMessage>

interface RpsMessageLogProvider {
    fun getMessageLog(sessionId: RpsSession.Id): RpsMessageLog
}

@Component
class InMemoryRpsMessageLogProvider : RpsMessageLogProvider {

    private val savedMessages = ConcurrentHashMap<RpsSession.Id, MutableList<String>>()

    override fun getMessageLog(sessionId: RpsSession.Id): RpsMessageLog =
        RpsMessageLogImpl(
            format = Json,
            sessionSavedMessages = savedMessages.getOrPut(sessionId) {
                Collections.synchronizedList(ArrayList())
            }
        )
}

private class RpsMessageLogImpl(
    private val format: StringFormat,
    private val sessionSavedMessages: MutableList<String>,
) : RpsMessageLog {

    override suspend fun saveMessages(
        messages: Collection<LoggedGameMessage<RpsPlayer.Human, RpsMessage>>,
    ) {
        sessionSavedMessages += messages.map {
            rpsMessageLogFormat.encodeToString(RpsLoggedGameMessageSerializer, it)
        }
    }

    override fun readAllMessages(): Flow<LoggedGameMessage<RpsPlayer.Human, RpsMessage>> =
        sessionSavedMessages
            .toList()
            .asFlow()
            .map { rpsMessageLogFormat.decodeFromString(RpsLoggedGameMessageSerializer, it) }
}

// Костыль, так как иерархия классов сложная
val rpsMessageLogFormat = Json {
    serializersModule = SerializersModule {
        polymorphic(RpsMessage::class) {
            // join game
            subclass(RpsJoinGameMessage.YouJoinedGame::class)
            subclass(RpsJoinGameMessage.SomebodyJoinedGame::class)
            subclass(JoinGameMessageError.YouAlreadyJoined::class)
            subclass(JoinGameMessageError.SessionIsFull::class)

            // change stage
            subclass(RpsChangeStageMessage.GameStarted::class)
            subclass(RpsChangeStageMessage.GameFinished::class)

            // submit answer
            subclass(RpsSubmitAnswerMessage.Submitted::class)
            subclass(RpsSubmitAnswerMessage.RoundEnded::class)
            subclass(SubmitAnswerMessageError.AnswerAlreadySubmitted::class)
        }
    }
}
