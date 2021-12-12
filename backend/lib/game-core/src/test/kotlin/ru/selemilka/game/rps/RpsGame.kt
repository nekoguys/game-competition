package ru.selemilka.game.rps

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Service
import ru.selemilka.game.core.base.Game
import ru.selemilka.game.core.base.MessageToPlayer
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.processor.RpsRootCommand
import ru.selemilka.game.rps.processor.RpsRootMessage
import ru.selemilka.game.rps.processor.RpsRootProcessor

@Configuration
@ComponentScan
class RpsGameConfiguration

@Service
class RpsGame(
    rpsRootProcessor: RpsRootProcessor,
) : Game<RpsPlayer, RpsRootCommand, RpsRootMessage>, AutoCloseable {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private val game = scope.Game(rpsRootProcessor::process)

    override suspend fun accept(player: RpsPlayer, command: RpsRootCommand) =
        game.accept(player, command)

    override fun getAnnouncements(): Flow<MessageToPlayer<RpsPlayer, RpsRootMessage>> =
        game.getAnnouncements()

    override fun getAnnouncements(player: RpsPlayer): Flow<MessageToPlayer<RpsPlayer, RpsRootMessage>> =
        game.getAnnouncements(player)

    override fun close() {
        job.complete()
    }
}
