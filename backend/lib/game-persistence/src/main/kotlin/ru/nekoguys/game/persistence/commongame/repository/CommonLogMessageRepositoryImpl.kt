package ru.nekoguys.game.persistence.commongame.repository

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Repository
import ru.nekoguys.game.core.session.LoggedGameMessage
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.commongame.repository.CommonLogMessageRepository
import ru.nekoguys.game.persistence.commongame.model.DbSessionMessage
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

@Repository
class CommonLogMessageRepositoryImpl(
    private val objectMapper: ObjectMapper,
    private val dbSessionMessageRepository: DbSessionMessageRepository,
) : CommonLogMessageRepository {

    private val logger = LoggerFactory.getLogger(CommonLogMessageRepositoryImpl::class.java)
    private val messagesInSessionCounters = ConcurrentHashMap<Long, AtomicLong>()

    override suspend fun saveMessages(
        sessionId: CommonSession.Id,
        messages: List<LoggedGameMessage<*, *>>,
    ) {
        // не очень эффективный способ получения
        val messagesCount = getSessionMessagesCount(sessionId)

        val dbSessionMessages = messages
            .map {
                DbSessionMessage(
                    id = null,
                    sessionId = sessionId.number,
                    seqNum = messagesCount.getAndIncrement(),
                    players = objectMapper.writeValueAsString(it.players),
                    message = objectMapper.writeValueAsString(it.messageBody),
                )
            }

        dbSessionMessageRepository
            .saveAll(dbSessionMessages)
            .toList()
            .also { logger.info("Saved game session messages $it to DB") }
    }

    private suspend fun getSessionMessagesCount(
        sessionId: CommonSession.Id,
    ): AtomicLong =
        messagesInSessionCounters
            .getOrPut(sessionId.number) {
                dbSessionMessageRepository
                    .countBySessionId(sessionId.number)
                    .let(::AtomicLong)
            }

    override fun <P, Msg> readAllMessages(
        sessionId: CommonSession.Id,
        playerClass: Class<P>,
        messageClass: Class<Msg>,
    ): Flow<LoggedGameMessage<P, Msg>> =
        dbSessionMessageRepository
            .getAllBySessionId(sessionId.number)
            .map {
                LoggedGameMessage(
                    players = parsePlayersSet(it.players, playerClass),
                    messageBody = parseMessage(it.message, messageClass)
                )
            }
            .also { logger.info("Loading all messages from session $sessionId") }

    private fun <P> parsePlayersSet(
        jsonString: String,
        playerClass: Class<P>,
    ): Set<P> =
        objectMapper
            .readValue(
                jsonString,
                objectMapper
                    .typeFactory
                    .constructCollectionType(MutableSet::class.java, playerClass),
            )
            ?: error("Expected valid players list, but got $jsonString")

    private fun <Msg> parseMessage(
        jsonString: String,
        messageClass: Class<Msg>,
    ): Msg =
        objectMapper
            .readValue(
                jsonString,
                messageClass
            )
            ?: error("Expected valid message, but got $jsonString")
}
