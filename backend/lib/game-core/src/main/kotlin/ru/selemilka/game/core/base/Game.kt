package ru.selemilka.game.core.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

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
interface Game<P, in Cmd : Command<P>, Msg, out A : Announcement<Msg>> :
    CommandAccepter<P, Cmd>,
    AnnouncementSource<Msg, A>

/**
 * Фабричный метод для создания игры
 *
 * Параметры задают поведение будущей игры:
 * * [processing] - как будет обрабатываться конкретный [Command]
 */
@Suppress("FunctionName")
suspend fun <P, Cmd : Command<P>, Msg, A : Announcement<Msg>> Game(
    processing: suspend (P, Cmd) -> List<A>,
): Game<P, Cmd, Msg, A> {
    val announcements = MutableSharedFlow<A>()

    val accepter = SimpleCommandAccepter<P, Cmd> { player, command ->
        processing(player, command)
    }

    val announcementSource = object : AnnouncementSource<Msg, A> {
        override fun getAnnouncements(): Flow<A> = announcements.asSharedFlow()
    }

    return object :
        Game<P, Cmd, Msg, A>,
        CommandAccepter<P, Cmd> by accepter,
        AnnouncementSource<Msg, A> by announcementSource {}
}
