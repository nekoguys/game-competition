package ru.selemilka.game.core.base

/**
 * Описание логики обработки команды.
 *
 * Команды обрабатываются параллельно в соответствии с результатом функции [getLocksFor]
 */
interface GameRule<P, in Cmd, out Msg> {
    suspend fun process(player: P, command: Cmd): List<GameMessage<P, Msg>>

    /**
     * Блокировки, которые нужно взять для обработки [command]
     *
     * Это необходимо для того, чтобы не допустить race condition'ы.
     */
    suspend fun getLocksFor(command: Cmd): LockedResources =
        LockedResources(
            shared = setOf(),
            unique = defaultUniqueResources,
        )
}

data class LockedResources(
    val shared: Set<Any>,
    val unique: Set<Any>,
)

private val defaultUniqueResources = setOf(Any())
