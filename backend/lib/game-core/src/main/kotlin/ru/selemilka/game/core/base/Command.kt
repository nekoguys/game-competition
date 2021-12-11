package ru.selemilka.game.core.base

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

/**
 * Действие, которое игрок [P] совершил в игре.
 *
 * Наследники образуют сложную иерархию.
 * Пример для игры "Конкуренция":
 * ```
 * Command
 * |   CompetitionCommand
 * |   |   JoinSessionCommand
 * |   |   PlayerTeamCommand
 * |   |   |   CreateTeam
 * |   |   |   JoinTeam
 * |   |   |   SendTeamMessage
 * |   |   StartGameCommand
 * |   |   TeacherCommand
 * |   |   |   BanPlayer
 * |   |   |   SendGameMessage
 * |   |   |   ChangeRoundLength
 * |   |   PlayerCompetitionCommand
 * |   |   |   ProduceGoods
 * |   |   TimerCommand
 * |   |   |   TimeOutCommand
 * ```
 *
 * [P] - Кто запросил действие в игре.
 *
 * Игроки образуют свою иерархию.
 * Пример для игры "Конкуренция":
 * ```
 * Player
 * |   CompetitionPlayer
 * |   |   Human - реальные пользователи, зарегистрированные в игре
 * |   |   |   Teacher
 * |   |   |   Player
 * |   |   |   |   Captain
 * |   |   |   BannedPlayer
 * |   |   Robot - внутренние сервисы, делающие вид, что тоже играют в игру
 * |   |   |   Timer - чтобы завершать раунд по таймеру
 */
interface Command<out P>

/**
 * Публичное API для получения команд [Cmd] от игроков.
 *
 * Классы-наследники задают, как именно получаются сообщения:
 * * обрабатываются ли сообщения параллельно
 * * какие сообщения обрабатываются последовательно
 * * и т.д.
 */
interface CommandAccepter<P, in Cmd : Command<P>> {
    /**
     * Эта функция приостанавливается до того момента,
     * пока команда не обработается.
     *
     * Если при обработке команда возникло исключение - оно бросается из этого метода
     */
    suspend fun accept(player: P, command: Cmd)
}

private data class Launch<P, out Cmd : Command<P>>(
    val player: P,
    val command: Cmd,
    val ack: CompletableDeferred<Unit>,
)
/**
 * Реализация [CommandAccepter], которая выполняет обрабатывает команды по очереди
 */
@Suppress("FunctionName")
suspend fun <P, Cmd : Command<P>> SimpleCommandAccepter(
    handler: suspend (P, Cmd) -> Unit,
): CommandAccepter<P, Cmd> = coroutineScope {
    val launches = Channel<Launch<P, Cmd>>()

    launch { channelReader(launches, handler) }

    object : CommandAccepter<P, Cmd> {
        override suspend fun accept(player: P, command: Cmd) {
            val ack = CompletableDeferred<Unit>()
            launches.send(Launch(player, command, ack))
            ack.await()
        }
    }
}

private suspend fun <P, Cmd : Command<P>> channelReader(
    launches: Channel<Launch<P, Cmd>>,
    handler: suspend (P, Cmd) -> Unit,
) {
    for ((command, player, ack) in launches) {
        runCatching { handler(command, player) }
            .onFailure { ex ->
                if (ex is CancellationException) {
                    throw ex
                }
            }
            .also(ack::completeWith)
    }
}
