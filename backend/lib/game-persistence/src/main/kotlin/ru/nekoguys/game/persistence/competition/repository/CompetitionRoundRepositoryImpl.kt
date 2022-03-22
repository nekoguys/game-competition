package ru.nekoguys.game.persistence.competition.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.ReactiveSelectOperation
import org.springframework.data.r2dbc.core.awaitOneOrNull
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria.where
import org.springframework.data.relational.core.query.Query
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import ru.nekoguys.game.entity.commongame.model.CommonSession
import ru.nekoguys.game.entity.competition.model.CompetitionRound
import ru.nekoguys.game.entity.competition.model.CompetitionRoundAnswer
import ru.nekoguys.game.entity.competition.model.withoutIncome
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundAnswerRepository
import ru.nekoguys.game.entity.competition.repository.CompetitionRoundRepository
import ru.nekoguys.game.persistence.competition.model.DbCompetitionRoundInfo

@Repository
class CompetitionRoundRepositoryImpl(
    private val databaseClient: DatabaseClient,
    private val r2dbcEntityTemplate: R2dbcEntityTemplate,
    private val competitionRoundAnswerRepository: CompetitionRoundAnswerRepository,
) : CompetitionRoundRepository {

    override suspend fun find(
        sessionId: CommonSession.Id,
        roundNumber: Int
    ): CompetitionRound? {
        val query = Query.query(
            where("session_id").`is`(sessionId.number)
                .and(where("roundNumber").`is`(roundNumber))
        )
        val dbCompetitionRound =
            createSelectOperation(query)
                .awaitOneOrNull() ?: return null

        val answers = competitionRoundAnswerRepository
            .findAll(sessionId, roundNumber)
            .toList()

        return createCompetitionRound(
            dbCompetitionRound,
            sessionId,
            answers,
        )
    }

    override suspend fun findAll(
        sessionIds: Collection<Long>
    ): List<CompetitionRound> = coroutineScope {
        val answers = async {
            competitionRoundAnswerRepository
                .findAll(sessionIds)
                .toList()
        }

        val query = Query.query(
            where("session_id").`in`(sessionIds),
        )
        val dbCompetitionRounds =
            createSelectOperation(query)
                .all()
                .asFlow()
                .toList()

        if (dbCompetitionRounds.isEmpty()) {
            answers.cancel()
            emptyList()
        } else {
            val answersBySessionAndRound = answers
                .await()
                .groupBy { Pair(it.sessionId, it.roundNumber) }
                .withDefault { emptyList() }

            dbCompetitionRounds
                .map { roundInfo ->
                    val sessionId = CommonSession.Id(roundInfo.sessionId)
                    createCompetitionRound(
                        dbCompetitionRound = roundInfo,
                        sessionId = sessionId,
                        answers = answersBySessionAndRound
                            .getValue(Pair(sessionId, roundInfo.roundNumber))
                    )
                }
        }
    }

    private suspend fun createSelectOperation(
        query: Query,
    ): ReactiveSelectOperation.TerminatingSelect<DbCompetitionRoundInfo> =
        r2dbcEntityTemplate
            .select<DbCompetitionRoundInfo>()
            .from("competition_round_infos")
            .matching(query)

    override suspend fun startRound(
        sessionId: CommonSession.Id,
        roundNumber: Int
    ) {
        databaseClient
            .sql(
                """
                INSERT INTO competition_round_infos (session_id, round_number, start_time)
                VALUES (:sessionId, :roundNumber, NOW())
                """.trimIndent()
            )
            .bind("sessionId", sessionId.number)
            .bind("roundNumber", roundNumber)
            .then()
            .awaitSingleOrNull()
    }

    override suspend fun endRound(
        sessionId: CommonSession.Id,
        roundNumber: Int,
    ) {
        databaseClient
            .sql(
                """
                UPDATE competition_round_infos
                SET end_time = NOW()
                WHERE session_id = :sessionId AND round_number = :roundNumber
                """.trimIndent()
            )
            .bind("sessionId", sessionId.number)
            .bind("roundNumber", roundNumber)
            .then()
            .awaitSingleOrNull()
    }

    override suspend fun updatePrice(
        sessionId: CommonSession.Id,
        roundNumber: Int,
        price: Double,
        defaultIncome: Double,
    ) {
        databaseClient
            .sql(
                """
                UPDATE competition_round_infos
                SET price = :price, default_income = :defaultIncome
                WHERE session_id = :sessionId AND round_number = :roundNumber
                """.trimIndent()
            )
            .bind("sessionId", sessionId.number)
            .bind("roundNumber", roundNumber)
            .bind("price", price)
            .bind("defaultIncome", defaultIncome)
            .then()
            .awaitSingleOrNull()
    }

    override suspend fun update(round: CompetitionRound) {
        TODO("Not yet implemented")
    }
}


private fun createCompetitionRound(
    dbCompetitionRound: DbCompetitionRoundInfo,
    sessionId: CommonSession.Id = CommonSession.Id(dbCompetitionRound.sessionId),
    answers: List<CompetitionRoundAnswer> = emptyList(),
): CompetitionRound =
    if (dbCompetitionRound.endTime == null) {
        CompetitionRound.Current(
            sessionId = sessionId,
            roundNumber = dbCompetitionRound.roundNumber,
            answers = answers.map { it.withoutIncome() },
            startTime = dbCompetitionRound.startTime
        )
    } else {
        CompetitionRound.Ended(
            sessionId = sessionId,
            roundNumber = dbCompetitionRound.roundNumber,
            answers = answers.map { it as CompetitionRoundAnswer.WithIncome },
            startTime = dbCompetitionRound.startTime,
            endTime = dbCompetitionRound.endTime!!,
            price = dbCompetitionRound.price!!,
            defaultIncome = dbCompetitionRound.defaultIncome!!,
        )
    }
