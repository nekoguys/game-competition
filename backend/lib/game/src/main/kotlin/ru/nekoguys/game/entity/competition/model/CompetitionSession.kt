package ru.nekoguys.game.entity.competition.model

import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.user.model.User
import java.time.LocalDateTime

/**
 * Такая сложная модель данных позволяет загружать из БД только нужные поля:
 *
 * ```
 * val sessionId = competitionSessionRepository.create(
 *     stage = CompetitionStage.Registration,
 *     creatorId = User.Id(13),
 *     settings = CompetitionSettings.Default,
 * )
 *
 * val session = competitionSessionRepository.load(
 *     id = sessionId,
 *     CompetitionSession.WithSettings,
 *     CompetitionSession.WithTeams,
 * )
 * val settings = session.settings
 * val teams = session.teams
 * // val creatorId = session.creatorId - не компилируется, так как это поле не запрашивалось
 *
 * competitionSessionRepository.update(
 *     oldSession = session,
 *     newSession = session.copy(
 *         settings = settings.copy(
 *             roundLength = 120,
 *         ),
 *     ),
 * )
 * ```
 */
sealed interface CompetitionSession {
    val id: CommonSession.Id

    fun copy(
        creatorId: User.Id? = null,
        settings: CompetitionSettings? = null,
        stage: CompetitionStage? = null,
        teams: List<CompetitionTeam>? = null,
    ): CompetitionSession

    sealed interface WithCommonFields : CompetitionSession {
        val creatorId: User.Id
        val lastModified: LocalDateTime

        companion object : CompetitionSessionFieldSelector<WithCommonFields>
    }

    sealed interface WithSettings : CompetitionSession {
        val settings: CompetitionSettings

        companion object : CompetitionSessionFieldSelector<WithSettings>
    }

    sealed interface WithStage : CompetitionSession {
        val stage: CompetitionStage

        companion object : CompetitionSessionFieldSelector<WithStage>
    }

    sealed interface WithTeams : CompetitionSession {
        val teams: List<CompetitionTeam>

        companion object : CompetitionSessionFieldSelector<WithTeams>
    }

    sealed interface Full : WithTeams, WithSettings, WithStage, WithCommonFields {
        companion object : CompetitionSessionFieldSelector<Full>
    }
}

sealed interface CompetitionSessionFieldSelector<in T : CompetitionSession>

/**
 * Фабричная функция для создания модели игровой сессии
 */
@Suppress("FunctionName")
fun CompetitionSession(
    id: CommonSession.Id,
    creatorId: User.Id? = null,
    lastModified: LocalDateTime? = null,
    settings: CompetitionSettings? = null,
    stage: CompetitionStage? = null,
    teams: List<CompetitionTeam>? = null,
): CompetitionSession =
    CompetitionSessionImpl(
        idOrNull = id,
        creatorIdOrNull = creatorId,
        lastModifiedOrNull = lastModified,
        settingsOrNull = settings,
        stageOrNull = stage,
        teamsOrNull = teams,
    )

data class CompetitionSessionImpl(
    val idOrNull: CommonSession.Id?,
    val creatorIdOrNull: User.Id? = null,
    val lastModifiedOrNull: LocalDateTime? = null,
    val settingsOrNull: CompetitionSettings? = null,
    val stageOrNull: CompetitionStage? = null,
    val teamsOrNull: List<CompetitionTeam>? = null,
) : CompetitionSession.Full {

    @Deprecated(
        "This property may throw if field is not set",
        ReplaceWith("idOrNull"),
    )
    override val id: CommonSession.Id
        get() = idOrNull ?: error("")

    @Deprecated(
        "This property may throw if field is not set",
        ReplaceWith("creatorIdOrNull"),
    )
    override val creatorId: User.Id
        get() = creatorIdOrNull ?: error("")

    @Deprecated(
        "This property may throw if field is not set",
        ReplaceWith("lastModifiedOrNull"),
    )
    override val lastModified: LocalDateTime
        get() = lastModifiedOrNull ?: error("")

    @Deprecated(
        "This property may throw if field is not set",
        ReplaceWith("settingsOrNull"),
    )
    override val settings: CompetitionSettings
        get() = settingsOrNull ?: error("")

    @Deprecated(
        "This property may throw if field is not set",
        ReplaceWith("stageOrNull"),
    )
    override val stage: CompetitionStage
        get() = stageOrNull ?: error("")

    @Deprecated(
        "This property may throw if field is not set",
        ReplaceWith("teamsOrNull"),
    )
    override val teams: List<CompetitionTeam>
        get() = teamsOrNull ?: error("")

    override fun copy(
        creatorId: User.Id?,
        settings: CompetitionSettings?,
        stage: CompetitionStage?,
        teams: List<CompetitionTeam>?,
    ): CompetitionSession = copy(
        creatorIdOrNull = creatorId,
        settingsOrNull = settings,
        stageOrNull = stage,
        teamsOrNull = teams,
    )
}
