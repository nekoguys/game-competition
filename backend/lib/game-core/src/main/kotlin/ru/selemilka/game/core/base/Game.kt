package ru.selemilka.game.core.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*

/**
 * Вообще игра состоит из трёх частей:
 * 1. приём команд от игроков. Валидация, проверки, может ли игрок сделать такое действие
 * 2. обработка команд в нужном порядке. При обработке команды игрокам отправляются несколько сообщений
 * 3. каждое сгенерированное сообщение отдаётся получателям
 *
 * 1. обработка команд
 * Игре приходит запрос Cmd
 * Игра ищет, кто сделал запрос, генерирует объект CommandRequest(cmd, player)
 * Если игрок не может сделать этот запрос, ему отправляется ошибка.
 * Если может - запрос передаётся в Executor.
 *
 * 2. отправка сообщений
 * Сообщения отправляются какой-то группе игроков (scope)
 * MessageSource для каждого сообщения ищет, кому оно отправлено, и раскладывает сообщения в свои топики.
 *
 * Итого от игрока
 */

/**
 * Публичное API игры.
 *
 * Пользователи класса [Game] могут:
 * * отправлять игре команды [Cmd] через метод [CommandQueue.accept]
 * * получать от игры сообщения [Msg] через метод [MessageSource.getAllMessages]
 *
 * Создать игру, зная, как обрабатываются запросы, можно фабричным методом [Game]
 */
interface Game<P, in Cmd : Command<P>, out Msg> :
    CommandQueue<P, Cmd>,
    MessageSource<P, Msg>

/**
 * Фабричный метод для создания игры
 *
 * Параметры задают поведение будущей игры:
 * * [commandTransform] - как будет обрабатываться конкретный [Command]
 */
@Suppress("FunctionName")
fun <P, Cmd : Command<P>, Msg> CoroutineScope.Game(
    commandTransform: suspend (P, Cmd) -> List<TargetedMessage<P, Msg>>,
): Game<P, Cmd, Msg> {
    val targetedMessages = MutableSharedFlow<TargetedMessage<P, Msg>>()

    val commandQueue = SimpleCommandQueue(
        onCommand = { player: P, command: Cmd ->
            for (message in commandTransform(player, command)) {
                targetedMessages.emit(message)
            }
        },
        onStop = {
            targetedMessages.emit(TerminalTargetedMessage)
        },
    )

    val messageSource = object : MessageSource<P, Msg> {
        override fun getAllMessages(): Flow<TargetedMessage<P, Msg>> =
            targetedMessages
                .asSharedFlow()
                .takeWhile { it !is TerminalTargetedMessage }

        override fun getMessages(player: P): Flow<Msg> =
            getAllMessages()
                .filter { it.player == player }
                .map { it.message }
    }

    return object :
        Game<P, Cmd, Msg>,
        CommandQueue<P, Cmd> by commandQueue,
        MessageSource<P, Msg> by messageSource {}
}

