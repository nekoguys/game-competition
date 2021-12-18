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
interface CommandQueue<P, in Cmd : Command<P>> {
    /**
     * Эта функция приостанавливается до того момента,
     * пока команда не обработается.
     *
     * Если при обработке команда возникло исключение - оно бросается из этого метода
     */
    suspend fun accept(player: P, command: Cmd)

    /**
     * Завершает приём команд объектом [CommandQueue]
     *
     * После вызова этой функции дальнейшие вызовы функции [accept]
     * бросают исключение [StoppedGameException].
     *
     * При вызове [stop] в очередь добавляется команда [StoppedGameException]
     * и прекращается дальнейший приём команд.
     */
    suspend fun stop()
}

class StoppedGameException : CancellationException()

/**
 * Реализация [CommandQueue], которая выполняет команды по одной в порядке очереди
 */
@Suppress("FunctionName")
fun <P, Cmd : Command<P>> CoroutineScope.SimpleCommandQueue(
    onCommand: suspend (P, Cmd) -> Unit,
    onStop: suspend () -> Unit,
): CommandQueue<P, Cmd> {
    val tasks = Channel<Task>()

    launch {
        for (task in tasks) {
            runCatching { task.action() }
                .onFailure { ex ->
                    if (ex is CancellationException) {
                        throw ex
                    }
                }
                .also(task.ack::completeWith)
        }
    }

    return object : CommandQueue<P, Cmd> {
        override suspend fun accept(player: P, command: Cmd) {
            val ack = CompletableDeferred<Unit>()
            tasks.send(Task(ack, action = { onCommand(player, command) }))
            ack.await()
        }

        override suspend fun stop() {
            val ack = CompletableDeferred<Unit>()
            tasks.send(Task(ack, action = { onStop() }))
            tasks.close(cause = StoppedGameException())
            ack.await()
        }
    }
}

class Task(
    val ack: CompletableDeferred<Unit>,
    val action: suspend () -> Unit,
)
