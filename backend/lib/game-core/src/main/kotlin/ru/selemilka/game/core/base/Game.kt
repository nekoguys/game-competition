package ru.selemilka.game.core.base

/**
 * Публичное API игры.
 *
 * Пользователи класса [Game] могут:
 * * отправлять игре сообщения [AnyAction] через метод [ActionConsumer.consume]
 * * получать от игры ответы [AnyReaction] через метод [ReactionProducer.allReactions]
 *
 * Что игра делает "за кулисами" - детали реализации.
 * Создать игру, зная детали реализации, можно фабричным методом [createGame]
 */
interface Game<in A : AnyAction, out R : AnyReaction> :
    ActionConsumer<A>,
    ReactionProducer<R>


/**
 * Реализация публичных API игры через делегацию.
 */
private class GameImpl<in A : AnyAction, out R : AnyReaction>(
    private val consumer: ActionConsumer<A>,
    private val producer: ReactionProducer<R>,
) : Game<A, R>,
    ActionConsumer<A> by consumer,
    ReactionProducer<R> by producer

/**
 * Фабричный метод для создания игры
 *
 * Параметры задают поведение будущей игры:
 * * [consumerBuilder] - как должны обрабатываться входящие [AnyAction]
 * * [processor] - как будет обрабатываться конкретный [AnyAction]
 * * [producer] - как о новых реакциях будут узнавать пользователи
 */
fun <A : AnyAction, R : AnyReaction> createGame(
    consumerBuilder: (OnActionCall<A>) -> ActionConsumer<A> = ::FairActionConsumer,
    processor: Processor<A, R>,
    producer: MutableReactionProducer<R> = SimpleReactionProducer(),
): Game<A, R> = GameImpl(
    consumer = consumerBuilder { sessionId, action ->
        // TODO: подумать над ретраями, ведь тут могут быть ошибки, связанные с БД
        val reactions = processor.process(sessionId, action)
        producer.shareReactions(reactions)
    },
    producer = producer,
)
