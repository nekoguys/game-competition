package ru.nekoguys.game.entity.competition.repository

import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionSession
import ru.nekoguys.game.entity.competition.model.CompetitionSessionFieldSelector
import ru.nekoguys.game.entity.competition.model.CompetitionSettings
import ru.nekoguys.game.entity.competition.model.CompetitionStage
import ru.nekoguys.game.entity.user.model.User

interface CompetitionSessionRepository {
    suspend fun create(
        userId: User.Id,
        settings: CompetitionSettings,
        stage: CompetitionStage,
    ): CompetitionSession

    suspend fun findAll(
        ids: Collection<Long>,
        fieldSelectors: Set<CompetitionSessionFieldSelector<*>>,
    ): List<CompetitionSession>

    suspend fun update(
        from: CompetitionSession,
        to: CompetitionSession,
    )

    suspend fun findIdsByCreatorId(
        creatorId: Long,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0,
    ): List<CommonSession.Id>

    suspend fun findIdsByParticipantId(
        participantId: Long,
        limit: Int = Int.MAX_VALUE,
        offset: Int = 0,
    ): List<CommonSession.Id>
}

suspend fun CompetitionSessionRepository.load(
    id: CommonSession.Id,
    fieldSelectors: Set<CompetitionSessionFieldSelector<*>>,
): CompetitionSession =
    findAll(listOf(id.number), fieldSelectors)
        .singleOrNull()
        ?: error("No competition session with id $id")

@Suppress(
    "UNCHECKED_CAST",
    "NOTHING_TO_INLINE",
)
suspend inline fun <T1, R> CompetitionSessionRepository.load(
    id: CommonSession.Id,
    t1: CompetitionSessionFieldSelector<T1>,
): R where
        T1 : CompetitionSession,
        R : T1 {
    return load(id, setOf(t1)) as R
}

@Suppress(
    "UNCHECKED_CAST",
    "BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER",
    "NOTHING_TO_INLINE",
)
suspend inline fun <T1, T2, R> CompetitionSessionRepository.load(
    id: CommonSession.Id,
    t1: CompetitionSessionFieldSelector<T1>,
    t2: CompetitionSessionFieldSelector<T2>,
): R where
        T1 : CompetitionSession,
        T2 : CompetitionSession,
        R : T1, R : T2 {
    return load(id, setOf(t1, t2)) as R
}

@Suppress(
    "UNCHECKED_CAST",
    "BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER",
    "NOTHING_TO_INLINE",
)
suspend inline fun <T1, T2, T3, R> CompetitionSessionRepository.load(
    id: CommonSession.Id,
    t1: CompetitionSessionFieldSelector<T1>,
    t2: CompetitionSessionFieldSelector<T2>,
    t3: CompetitionSessionFieldSelector<T3>,
): R where
        T1 : CompetitionSession,
        T2 : CompetitionSession,
        T3 : CompetitionSession,
        R : T1, R : T2, R : T3 {
    return load(id, setOf(t1, t2, t3)) as R
}

@Suppress(
    "UNCHECKED_CAST",
    "BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER",
    "NOTHING_TO_INLINE",
)
suspend inline fun <T1, T2, T3, T4, R> CompetitionSessionRepository.load(
    id: CommonSession.Id,
    t1: CompetitionSessionFieldSelector<T1>,
    t2: CompetitionSessionFieldSelector<T2>,
    t3: CompetitionSessionFieldSelector<T3>,
    t4: CompetitionSessionFieldSelector<T4>,
): R where
        T1 : CompetitionSession,
        T2 : CompetitionSession,
        T3 : CompetitionSession,
        T4 : CompetitionSession,
        R : T1, R : T2, R : T3, R : T4 {
    return load(id, setOf(t1, t2, t3, t4)) as R
}

@Suppress("UNCHECKED_CAST")
suspend fun <T1 : CompetitionSession> CompetitionSessionRepository.findAll(
    ids: Collection<Long>,
    t1: CompetitionSessionFieldSelector<T1>,
): List<T1> {
    return findAll(ids, setOf(t1)) as List<T1>
}

@Suppress(
    "UNCHECKED_CAST",
    "BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER",
    "NOTHING_TO_INLINE",
)
suspend inline fun <T1, T2, R> CompetitionSessionRepository.findAll(
    ids: Collection<Long>,
    t1: CompetitionSessionFieldSelector<T1>,
    t2: CompetitionSessionFieldSelector<T2>,
): List<R> where
        T1 : CompetitionSession,
        T2 : CompetitionSession,
        R : T1, R : T2 {
    return findAll(ids, setOf(t1, t2)) as List<R>
}

@Suppress(
    "UNCHECKED_CAST",
    "BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER",
    "NOTHING_TO_INLINE",
)
suspend inline fun <T1, T2, T3, R> CompetitionSessionRepository.findAll(
    ids: Collection<Long>,
    t1: CompetitionSessionFieldSelector<T1>,
    t2: CompetitionSessionFieldSelector<T2>,
    t3: CompetitionSessionFieldSelector<T3>,
): List<R> where
        T1 : CompetitionSession,
        T2 : CompetitionSession,
        T3 : CompetitionSession,
        R : T1, R : T2, R : T3 {
    return findAll(ids, setOf(t1, t2, t3)) as List<R>
}

@Suppress(
    "UNCHECKED_CAST",
    "BOUNDS_NOT_ALLOWED_IF_BOUNDED_BY_TYPE_PARAMETER",
    "NOTHING_TO_INLINE",
)
suspend inline fun <T1, T2, T3, T4, R> CompetitionSessionRepository.findAll(
    ids: Collection<Long>,
    t1: CompetitionSessionFieldSelector<T1>,
    t2: CompetitionSessionFieldSelector<T2>,
    t3: CompetitionSessionFieldSelector<T3>,
    t4: CompetitionSessionFieldSelector<T4>,
): List<R> where
        T1 : CompetitionSession,
        T2 : CompetitionSession,
        T3 : CompetitionSession,
        T4 : CompetitionSession,
        R : T1, R : T2, R : T3, R : T4 {
    return findAll(ids, setOf(t1, t2, t3, t4)) as List<R>
}
