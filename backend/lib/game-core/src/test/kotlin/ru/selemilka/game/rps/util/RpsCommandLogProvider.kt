package ru.selemilka.game.rps.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.StringFormat
import kotlinx.serialization.json.Json
import org.springframework.stereotype.Component
import ru.selemilka.game.core.session.GameCommandRequestLog
import ru.selemilka.game.core.session.LoggedGameCommand
import ru.selemilka.game.rps.model.RpsPlayer
import ru.selemilka.game.rps.model.RpsSession
import ru.selemilka.game.rps.rule.RpsCommand
import java.util.*
import java.util.concurrent.ConcurrentHashMap

interface RpsCommandLog : GameCommandRequestLog<RpsPlayer, RpsCommand>

interface RpsCommandLogProvider {
    fun getCommandLog(sessionId: RpsSession.Id): RpsCommandLog
}

@Component
class InMemoryRpsCommandLogProvider : RpsCommandLogProvider {

    private val savedCommands = ConcurrentHashMap<RpsSession.Id, MutableList<String>>()

    override fun getCommandLog(sessionId: RpsSession.Id): RpsCommandLog =
        RpsCommandLogImpl(
            format = Json,
            sessionSavedCommands = savedCommands.getOrPut(sessionId) {
                Collections.synchronizedList(ArrayList())
            }
        )
}

private class RpsCommandLogImpl(
    private val format: StringFormat,
    private val sessionSavedCommands: MutableList<String>,
) : RpsCommandLog {

    override suspend fun saveCommand(
        command: LoggedGameCommand<RpsPlayer, RpsCommand>,
    ) {
        sessionSavedCommands += format.encodeToString(RpsLoggedGameCommandSerializer, command)
    }

    override fun readAllCommands(): Flow<LoggedGameCommand<RpsPlayer, RpsCommand>> =
        sessionSavedCommands
            .toList()
            .asFlow()
            .map { format.decodeFromString(RpsLoggedGameCommandSerializer, it) }

}
