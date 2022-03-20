package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.ReactiveSelectOperation
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.bind
import org.springframework.stereotype.Repository
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionRoundAnswer
import ru.nekoguys.game.entity.competition.model.CompetitionTeam
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundAnswerRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionRoundAnswer

@Repository
class CompetitionRoundAnswerRepositoryImpl(
    private val databaseClient: DatabaseClient,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
) : CompetitionRoundAnswerRepository {

    override suspend fun save(
        answer: CompetitionRoundAnswer
    ) {
        databaseClient
            .sql(
                """
                    INSERT INTO competition_round_answers (session_id, round_number, team_id, value, income) 
                    VALUES (:sessionId, :roundNumber, :teamId, :value, :income)
                """.trimIndent()
            )
            .bind("sessionId", answer.sessionId.number)
            .bind("roundNumber", answer.roundNumber)
            .bind("teamId", answer.teamId.number)
            .bind("value", answer.production)
            .bind(
                name = "income",
                value = (answer as? CompetitionRoundAnswer.WithIncome)?.income
            )
            .then()
            .awaitSingleOrNull()
    }

    override suspend fun update(
        answer: CompetitionRoundAnswer
    ) {
        databaseClient
            .sql(
                """
                    UPDATE competition_round_answers
                    SET value = :value, income = :income
                    WHERE session_id = :sessionId
                    AND round_number = :roundNumber
                    AND team_id = :teamId
                """.trimIndent()
            )
            .bind("sessionId", answer.sessionId.number)
            .bind("roundNumber", answer.roundNumber)
            .bind("teamId", answer.teamId.number)
            .bind("value", answer.production)
            .bind(
                name = "income",
                value = (answer as? CompetitionRoundAnswer.WithIncome)?.income
            )
            .then()
            .awaitSingleOrNull()
    }

    override suspend fun find(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
        roundNumber: Int
    ): CompetitionRoundAnswer? {
        val query = Query
            .query(
                where("session_id").`is`(sessionId.number)
                    .and(where("team_id").`is`(teamId.number))
                    .and(where("roundNumber").`is`(roundNumber))
            )
            .limit(1)

        return createSelectOperation(query)
            .awaitOneOrNull()
            ?.let { createCompetitionRoundAnswer(it, sessionId, teamId) }
    }

    override fun findAll(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id
    ): Flow<CompetitionRoundAnswer> {
        val query = Query
            .query(
                where("session_id").`is`(sessionId.number)
                    .and(where("team_id").`is`(teamId.number))
            )

        return createSelectOperation(query)
            .all()
            .asFlow()
            .map { db -> createCompetitionRoundAnswer(db, sessionId, teamId) }
    }

    override fun findAll(
        sessionId: CommonSession.Id,
        roundNumber: Int,
    ): Flow<CompetitionRoundAnswer> {
        val query = Query
            .query(
                where("session_id").`is`(sessionId.number)
                    .and(where("round_number").`is`(roundNumber))
            )

        return createSelectOperation(query)
            .all()
            .asFlow()
            .map { db -> createCompetitionRoundAnswer(db, sessionId) }
    }

    override fun findAll(
        sessionIds: Collection<Long>
    ): Flow<CompetitionRoundAnswer> {
        val query = Query.query(
            where("session_id").`in`(sessionIds)
        )

        return createSelectOperation(query)
            .all()
            .asFlow()
            .map { db -> createCompetitionRoundAnswer(db) }
    }

    private fun createSelectOperation(
        query: Query
    ): ReactiveSelectOperation.TerminatingSelect<DbCompetitionRoundAnswer> =
        r2dbcEntityTemplate
            .select<DbCompetitionRoundAnswer>()
            .from("competition_round_answers")
            .matching(query)

    override suspend fun delete(
        sessionId: CommonSession.Id,
        teamId: CompetitionTeam.Id,
        roundNumber: Int,
    ) {
        TODO()
    }
}


private fun createCompetitionRoundAnswer(
    db: DbCompetitionRoundAnswer,
    sessionId: CommonSession.Id = CommonSession.Id(db.sessionId),
    teamId: CompetitionTeam.Id = CompetitionTeam.Id(db.teamId),
) = if (db.income == null) {
    CompetitionRoundAnswer.WithoutIncome(
        sessionId = sessionId,
        teamId = teamId,
        roundNumber = db.roundNumber,
        production = db.value,
    )
} else {
    CompetitionRoundAnswer.WithIncome(
        sessionId = sessionId,
        teamId = teamId,
        roundNumber = db.roundNumber,
        production = db.value,
        income = db.income!!
    )
}
