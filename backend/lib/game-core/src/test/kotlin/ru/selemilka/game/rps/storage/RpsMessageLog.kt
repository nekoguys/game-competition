package ru.selemilka.game.rps.storage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import org.springframework.stereotype.Component
import ru.selemilka.game.rps.RpsGameMessage
import ru.selemilka.game.rps.model.RpsSession
import ru.selemilka.game.rps.rule.RpsMessage
import ru.selemilka.game.rps.util.format
import java.util.concurrent.ConcurrentHashMap

interface RpsMessageLog {
    suspend fun save(
        sessionId: RpsSession.Id,
        messages: Flow<RpsGameMessage<RpsMessage>>,
    )

    fun load(sessionId: RpsSession.Id): Flow<RpsGameMessage<RpsMessage>>
}

@Component
class RpsMessageInMemoryLog : RpsMessageLog {

    private val savedMessages = ConcurrentHashMap<RpsSession.Id, MutableList<String>>()

    override suspend fun save(
        sessionId: RpsSession.Id,
        messages: Flow<RpsGameMessage<RpsMessage>>,
    ) {
        messages
            .map { format.encodeToString(it) }
            .flowOn(Dispatchers.IO)
            .collect { newMessage ->
                savedMessages.compute(sessionId) { _, savedMessages ->
                    if (savedMessages == null) {
                        mutableListOf(newMessage)
                    } else {
                        savedMessages += newMessage
                        savedMessages
                    }
                }
            }
    }

    override fun load(sessionId: RpsSession.Id): Flow<RpsGameMessage<RpsMessage>> {
        return flow {
            lateinit var resultList: List<String>
            withContext(Dispatchers.IO) {
                savedMessages.compute(sessionId) { _, messages ->
                    resultList = messages.orEmpty().toMutableList()
                    // добавляем в мапу копию списка
                    messages
                }
            }

            for (serializedMessage in resultList) {
                emit(format.decodeFromString(serializedMessage))
            }
        }
    }
}
