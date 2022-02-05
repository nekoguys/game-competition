package ru.nekoguys.game.entity.competition

import kotlinx.coroutines.flow.Flow
import org.springframework.stereotype.Service
import ru.nekoguys.game.core.session.GameSession
import ru.nekoguys.game.core.session.accept
import ru.nekoguys.game.core.session.createGameSession
import ru.nekoguys.game.core.session.getMessages
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionBasePlayer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.repository.CompetitionPlayerRepository
import ru.nekoguys.game.entity.competition.rule.CompetitionCommand
import ru.nekoguys.game.entity.competition.rule.CompetitionMessage
import ru.nekoguys.game.entity.competition.rule.CompetitionRootRule
import ru.nekoguys.game.entity.user.model.User
import java.util.concurrent.ConcurrentHashMap

typealias CompetitionLaunchedSession =
        GameSession<CompetitionBasePlayer, CompetitionCommand, CompetitionTeam.Id, CompetitionMessage>

@Service
class CompetitionProcessService(
    private val competitionPlayerRepository: CompetitionPlayerRepository,
    private val rootRule: CompetitionRootRule,
) {
    private val launchedSessions =
        ConcurrentHashMap<CommonSession.Id, CompetitionLaunchedSession>()

    suspend fun acceptCommand(
        sessionId: CommonSession.Id,
        user: User,
        command: CompetitionCommand,
    ) {
        launchedSessions
            .getOrPut(sessionId) { launchGameSession(sessionId) }
            .accept(
                player = competitionPlayerRepository.load(sessionId, user),
                command = command,
            )
    }

    fun getAllMessagesForUser(
        sessionId: CommonSession.Id,
        user: User,
    ): Flow<CompetitionMessage> =
        launchedSessions
            .getOrPut(sessionId) { launchGameSession(sessionId) }
            .getMessages(user)

    private fun launchGameSession(
        sessionId: CommonSession.Id,
    ): CompetitionLaunchedSession =
        createGameSession(
            rule = rootRule,
            onClose = { launchedSessions.remove(sessionId) }
        )
}
