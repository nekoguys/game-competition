package ru.nekoguys.game.core.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.nekoguys.game.core.GameCommandRequest
import ru.nekoguys.game.core.GameMessage
import ru.nekoguys.game.core.GameMessageImpl
import ru.nekoguys.game.core.GameRule
import java.util.concurrent.atomic.AtomicInteger

internal class BaseGameSession<in P, in Cmd, P2, Msg>(
    private val rule: GameRule<P, Cmd, P2, Msg>,
    replay: Int,
) : InternalGameSession<P, Cmd, P2, Msg> {

    private val gameMessages =
        MutableSharedFlow<IndexedValue<GameMessage<P2, Msg>>>(replay = replay)
    private val lastMessageIndex = AtomicInteger()
    private val resourceLockSupport = ResourceLockProvider()

    override suspend fun acceptAndReturnMessages(
        request: GameCommandRequest<P, Cmd>,
    ): List<GameMessage<P2, Msg>> {
        val player = request.player
        val command = request.command
        return resourceLockSupport
            .useLocks(rule.getLocksFor(command)) {
                processWithoutSynchronization(command, player)
            }
            .also { messages -> shareMessages(messages) }
    }

    private suspend fun processWithoutSynchronization(
        command: Cmd,
        player: P,
    ): List<GameMessage<P2, Msg>> {
        logger.info("Started processing command $command by player $player")
        val res = rule.process(player, command)
        logger.info("Ended processing command $command by player $player")
        return res
    }

    override suspend fun shareMessages(messages: Collection<GameMessage<P2, Msg>>) {
        for (message in messages) {
            gameMessages.emit(message.withNextIndex())
        }
    }

    override fun getAllMessagesIndexed(): Flow<IndexedValue<GameMessage<P2, Msg>>> =
        gameMessages.asSharedFlow()

    private fun <T> T.withNextIndex(): IndexedValue<T> =
        IndexedValue(
            index = if (this is GameMessageImpl<*, *>) {
                lastMessageIndex.getAndIncrement()
            } else {
                -1
            },
            value = this,
        )

    private companion object {
        val logger: Logger = LoggerFactory.getLogger(BaseGameSession::class.java)
    }
}
