package ru.selemilka.game.core.base

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter

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
 * * отправлять игре команды [Command] через метод [CommandConsumer.consume]
 * * получать от игры сообщения [Message] через метод [AnnouncementSource.getAnnouncements]
 *
 * Создать игру, зная, как обрабатываются запросы, можно фабричным методом [Game]
 */
interface Game<P, in Cmd : Command<P>, out Msg> :
    CommandAccepter<P, Cmd>,
    AnnouncementSource<P, Msg>

/**
 * Фабричный метод для создания игры
 *
 * Параметры задают поведение будущей игры:
 * * [processing] - как будет обрабатываться конкретный [Command]
 */
@Suppress("FunctionName")
fun <P, Cmd : Command<P>, Msg> CoroutineScope.Game(
    processing: suspend (P, Cmd) -> List<MessageToPlayer<P, Msg>>,
): Game<P, Cmd, Msg> {
    val announcements = MutableSharedFlow<MessageToPlayer<P, Msg>>()

    val accepter = SimpleCommandAccepter<P, Cmd> { player, command ->
        processing(player, command)
    }

    val announcementSource = object : AnnouncementSource<P, Msg> {
        override fun getAnnouncements(): Flow<MessageToPlayer<P, Msg>> =
            announcements.asSharedFlow()

        override fun getAnnouncements(player: P): Flow<MessageToPlayer<P, Msg>> =
            getAnnouncements()
                .filter { it.player == player }
    }

    return object :
        Game<P, Cmd, Msg>,
        CommandAccepter<P, Cmd> by accepter,
        AnnouncementSource<P, Msg> by announcementSource {}
}
