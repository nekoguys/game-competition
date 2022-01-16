package ru.selemilka.game.core.base

/**
 * Запрос игрока на выполнение команды.
 *
 * Существуют два вида таких запросов:
 * * созданные через фабричную функцию [GameCommandRequest] - обычный запрос
 * * [CloseGameSessionRequest] - специальный запрос, при обработке которого сессия завершается
 */

sealed class GameCommandRequest<out P, out Cmd> {
    abstract val player: P
    abstract val command: Cmd
}

/**
 * Запрос игрока на выполнение команды
 *
 * Скорее всего наследники [Cmd] образуют сложную иерархию.
 * Пример для игры "Конкуренция":
 * ```
 * CompetitionCommand
 * |   JoinSessionCommand
 * |   PlayerTeamCommand
 * |   |   CreateTeam
 * |   |   JoinTeam
 * |   |   SendTeamAnnouncement
 * |   StartGameCommand
 * |   TeacherCommand
 * |   |   BanPlayer
 * |   |   SendGameMessage
 * |   |   ChangeRoundLength
 * |   PlayerCompetitionCommand
 * |   |   ProduceGoods
 * |   TimerCommand
 * |   |   TimeOutCommand
 * ```
 *
 * Игроки [P] тоже образуют свою иерархию.
 * Пример для игры "Конкуренция":
 * ```
 * CompetitionPlayer
 * |   Human - реальные пользователи, зарегистрированные в игре
 * |   |   Teacher
 * |   |   Player
 * |   |   |   Captain
 * |   |   BannedPlayer
 * |   Internal - внутренние сервисы, которым тоже можно отправлять команды
 * |   |   StageChanger -
 * |   |   Timer - чтобы завершать раунд по таймеру
 * ```
 */
@Suppress("FunctionName")
fun <P, Cmd> GameCommandRequest(
    player: P,
    command: Cmd,
): GameCommandRequest<P, Cmd> = GameCommandRequestImpl(player, command)

/**
 * Запрос, отправляемый в игровую сессию для её закрытия.
 *
 * При получении этого запроса игровая сессия перестаёт отправлять сообщения и принимать команды.
 * [CloseGameSessionRequest] можно отправлять в любой игре,
 * так как он реализует интерфейс `GameCommandRequest<Nothing, Nothing>`
 */
object CloseGameSessionRequest : GameCommandRequest<Nothing, Nothing>() {
    override val player: Nothing
        get() = error("${javaClass.name} doesn't have a message, because this request is internal and can be used only with closeable game sessions.")

    override val command: Nothing
        get() = error("${javaClass.name} doesn't have a command, because this request is internal and can be used only with closeable game sessions.")
}

private data class GameCommandRequestImpl<P, Cmd>(
    override val player: P,
    override val command: Cmd,
) : GameCommandRequest<P, Cmd>()
