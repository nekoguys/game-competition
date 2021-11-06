package ru.selemilka.game.core.stages

import ru.selemilka.game.core.base.AnyAction
import ru.selemilka.game.core.base.SessionId
import ru.selemilka.game.core.base.TypedProcessor
import kotlin.reflect.KClass

interface StageStorage<S : Enum<S>> {
    suspend fun currentStage(id: SessionId): S
    suspend fun changeStage(id: SessionId, newStage: S): Result<Unit>
}

/**
 * TODO: может быть сделать этот процессор похожим на [ru.selemilka.game.core.base.CompositeProcessor]?
 *  И принимать как параметр `Map<Stage, Processor>`
 */
class CheckingStageProcessor<S : Enum<S>>(
    private val actionWhitelist: Map<S, Collection<KClass<out AnyAction>>>,
    private val stageStorage: StageStorage<S>,
) : TypedProcessor<AnyAction, Nothing> {
    // TODO: Этот процессор поддерживает все типы запросов, тут не нужно что-либо фильтровать
    //       И actionClass тоже указывать не нужно
    override val actionClass = AnyAction::class

    override suspend fun process(id: SessionId, action: AnyAction): List<Nothing> {
        val current = stageStorage.currentStage(id)
        val allowedActions = actionWhitelist[current]

        return if (allowedActions != null && isAllowed(allowedActions, action)) {
            error("Invalid stage $current for action $action")
        } else {
            emptyList()
        }
    }

    private fun isAllowed(
        allowedActions: Collection<KClass<out AnyAction>>,
        action: AnyAction,
    ): Boolean = allowedActions.any { klass -> klass.isInstance(action) }
}
