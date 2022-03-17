package ru.nekoguys.game.persistence.competition.model

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.domain.Persistable
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import ru.nekoguys.game.entity.competition.model.CompetitionStage

@Table("competition_game_sessions")
data class DbCompetitionSession(
    @Id
    @Column("id")
    var sessionId: Long?,
    var stage: DbCompetitionStage,
    var lastRound: Int?,
) : Persistable<Long> {

    @Transient
    private var isNew: Boolean = sessionId == null

    fun asNew(): DbCompetitionSession =
        apply { isNew = true }

    override fun isNew(): Boolean = isNew

    override fun getId(): Long? = sessionId
}

enum class DbCompetitionStage {
    DRAFT,
    REGISTRATION,
    WAITING_ROUND_START,
    IN_PROGRESS,
    ENDED,
    UNKNOWN,
}

fun CompetitionStage.extractDbCompetitionStage() =
    when (this) {
        is CompetitionStage.Draft -> DbCompetitionStage.DRAFT
        is CompetitionStage.Registration -> DbCompetitionStage.REGISTRATION
        is CompetitionStage.WaitingStart -> DbCompetitionStage.WAITING_ROUND_START
        is CompetitionStage.InProcess -> DbCompetitionStage.IN_PROGRESS
        is CompetitionStage.Ended -> DbCompetitionStage.ENDED
    }

fun CompetitionStage.extractDbLastRound(): Int? =
    when (this) {
        is CompetitionStage.InProcess -> round
        is CompetitionStage.WaitingStart -> round
        else -> null
    }

fun DbCompetitionSession.extractCompetitionStage() =
    when (stage) {
        DbCompetitionStage.DRAFT -> CompetitionStage.Draft
        DbCompetitionStage.REGISTRATION -> CompetitionStage.Registration
        DbCompetitionStage.WAITING_ROUND_START ->
            CompetitionStage.WaitingStart(lastRound!!)
        DbCompetitionStage.IN_PROGRESS ->
            CompetitionStage.InProcess(lastRound!!)
        DbCompetitionStage.ENDED -> CompetitionStage.Ended
        DbCompetitionStage.UNKNOWN -> error("Got unknown competition stage")
    }
