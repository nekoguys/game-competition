package ru.nekoguys.game.core.rps.test

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.dropWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.nekoguys.game.core.rps.RpsGameCoreTest
import ru.nekoguys.game.core.rps.RpsGameService
import ru.nekoguys.game.core.rps.RpsGameSession
import ru.nekoguys.game.core.rps.model.RpsPlayer
import ru.nekoguys.game.core.rps.model.RpsSessionSettings
import ru.nekoguys.game.core.rps.model.Turn
import ru.nekoguys.game.core.rps.rule.*
import ru.nekoguys.game.core.session.accept
import ru.nekoguys.game.core.session.close
import ru.nekoguys.game.core.session.getMessages

@RpsGameCoreTest
class SubmitAnswerRuleTest @Autowired constructor(
    private val gameService: RpsGameService,
) {


    lateinit var session: RpsGameSession
    lateinit var firstPlayer: RpsPlayer
    lateinit var secondPlayer: RpsPlayer

    @BeforeEach
    fun before(): Unit = runBlocking {
        val sessionSettings = RpsSessionSettings(maxPlayers = 2)
        session = gameService.startSession(sessionSettings)

        firstPlayer = RpsPlayer.Human(session.id, "Amogus")
        session.accept(firstPlayer, RpsCommand.JoinGame)

        secondPlayer = RpsPlayer.Human(session.id, "Biba")
        session.accept(secondPlayer, RpsCommand.JoinGame)
    }

    @Test
    fun `can submit answer`(): Unit = runBlocking {
        session.accept(firstPlayer, RpsCommand.SubmitAnswer(turn = Turn.PAPER))
        session.close()

        val messages = session.getMessages(firstPlayer)
            .dropUntilFound(RpsChangeStageMessage.GameStarted)
            .toList()

        assertThat(messages)
            .containsExactly(
                RpsSubmitAnswerMessage.Submitted
            )
    }

    @Test
    fun `cannot submit answer twice`(): Unit = runBlocking {
        session.accept(firstPlayer, RpsCommand.SubmitAnswer(turn = Turn.PAPER))
        session.accept(firstPlayer, RpsCommand.SubmitAnswer(turn = Turn.SCISSORS))
        session.close()

        val messages = session.getMessages(firstPlayer)
            .dropUntilFound(RpsChangeStageMessage.GameStarted)
            .toList()

        assertThat(messages)
            .containsExactly(
                RpsSubmitAnswerMessage.Submitted,
                SubmitAnswerMessageError.AnswerAlreadySubmitted,
            )
    }

    @Test
    fun `can win in the game`(): Unit = runBlocking {
        session.accept(firstPlayer, RpsCommand.SubmitAnswer(turn = Turn.PAPER))
        session.accept(secondPlayer, RpsCommand.SubmitAnswer(turn = Turn.SCISSORS))

        val firstPlayerMessages = session.getMessages(firstPlayer)
            .dropUntilFound(RpsChangeStageMessage.GameStarted)
            .toList()
        val secondPlayerMessages = session.getMessages(secondPlayer)
            .dropUntilFound(RpsChangeStageMessage.GameStarted)
            .toList()

        assertThat(firstPlayerMessages)
            .containsExactly(
                RpsSubmitAnswerMessage.Submitted,
                RpsSubmitAnswerMessage.RoundEnded(RoundResult.YOU_LOST),
                RpsChangeStageMessage.GameFinished,
            )
        assertThat(secondPlayerMessages)
            .containsExactly(
                RpsSubmitAnswerMessage.Submitted,
                RpsSubmitAnswerMessage.RoundEnded(RoundResult.YOU_WON),
                RpsChangeStageMessage.GameFinished,
            )
    }

    @Test
    fun `round can end in a draw`(): Unit = runBlocking {
        session.accept(firstPlayer, RpsCommand.SubmitAnswer(turn = Turn.PAPER))
        session.accept(secondPlayer, RpsCommand.SubmitAnswer(turn = Turn.PAPER))
        session.close()

        val firstPlayerMessages = session.getMessages(firstPlayer)
            .dropUntilFound(RpsChangeStageMessage.GameStarted)
            .toList()
        val secondPlayerMessages = session.getMessages(secondPlayer)
            .dropUntilFound(RpsChangeStageMessage.GameStarted)
            .toList()

        assertThat(firstPlayerMessages)
            .containsExactly(
                RpsSubmitAnswerMessage.Submitted,
                RpsSubmitAnswerMessage.RoundEnded(RoundResult.DRAW),
            )
        assertThat(secondPlayerMessages)
            .containsExactly(
                RpsSubmitAnswerMessage.Submitted,
                RpsSubmitAnswerMessage.RoundEnded(RoundResult.DRAW),
            )
    }

    fun <T> Flow<T>.dropUntilFound(element: T): Flow<T> =
        dropWhile { it != element }.drop(1)
}
