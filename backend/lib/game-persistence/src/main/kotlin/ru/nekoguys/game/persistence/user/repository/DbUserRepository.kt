package ru.nekoguys.game.persistence.user.repository

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository
import ru.nekoguys.game.persistence.user.model.DbUser

@Repository
interface DbUserRepository : CoroutineCrudRepository<DbUser, Long> {
    @Suppress("SpringDataRepositoryMethodReturnTypeInspection")
    suspend fun findByEmail(email: String): DbUser?

    @Query(
        """
        SELECT * 
        FROM users AS u
        WHERE u.email ILIKE '%' || :query || '%'
           OR u.first_name || ' ' || u.second_name ILIKE '%' || :query || '%'
           OR u.second_name || ' ' || u.first_name ILIKE '%' || :query || '%'
        ORDER BY u.email
        LIMIT :limit
        OFFSET :offset
    """
    )
    fun findByQuery(
        query: String,
        offset: Int,
        limit: Int,
    ): Flow<DbUser>
}
