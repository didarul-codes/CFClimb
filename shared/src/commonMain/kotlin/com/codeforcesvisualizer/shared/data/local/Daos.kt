package com.codeforcesvisualizer.shared.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ContestDao {
    /** Newest first, matching the order of the contest.list API. */
    @Query("SELECT * FROM contests ORDER BY startTimeSeconds DESC")
    abstract fun observeAll(): Flow<List<ContestEntity>>

    @Query("SELECT * FROM contests WHERE id = :id")
    abstract fun observeById(id: Int): Flow<ContestEntity?>

    @Query(
        "SELECT * FROM contests WHERE name LIKE '%' || :key || '%' OR type LIKE '%' || :key || '%' " +
            "ORDER BY startTimeSeconds DESC"
    )
    abstract suspend fun search(key: String): List<ContestEntity>

    /** Swaps the whole list in one transaction, so observers never see it half-written. */
    @Transaction
    open suspend fun replaceAll(contests: List<ContestEntity>) {
        deleteAll()
        upsertAll(contests)
    }

    @Query("DELETE FROM contests")
    protected abstract suspend fun deleteAll()

    @Upsert
    protected abstract suspend fun upsertAll(contests: List<ContestEntity>)
}

@Dao
abstract class ProfileDao {
    @Query("SELECT * FROM users WHERE handleKey = :handleKey")
    abstract fun observeUser(handleKey: String): Flow<UserEntity?>

    @Upsert
    abstract suspend fun upsertUser(user: UserEntity)

    /** Chronological, matching the user.rating API. */
    @Query("SELECT * FROM rating_changes WHERE handleKey = :handleKey ORDER BY ratingUpdateTimeSeconds")
    abstract fun observeRatingChanges(handleKey: String): Flow<List<RatingChangeEntity>>

    /** Newest first, matching the user.status API. */
    @Query("SELECT * FROM submissions WHERE handleKey = :handleKey ORDER BY id DESC")
    abstract fun observeSubmissions(handleKey: String): Flow<List<SubmissionEntity>>

    @Query("SELECT * FROM fetch_times WHERE `key` = :key")
    abstract fun observeFetchTime(key: String): Flow<FetchTimeEntity?>

    @Transaction
    open suspend fun replaceRatingChanges(handleKey: String, changes: List<RatingChangeEntity>, fetchTime: FetchTimeEntity) {
        deleteRatingChanges(handleKey)
        insertRatingChanges(changes)
        upsertFetchTime(fetchTime)
    }

    @Transaction
    open suspend fun replaceSubmissions(handleKey: String, submissions: List<SubmissionEntity>, fetchTime: FetchTimeEntity) {
        deleteSubmissions(handleKey)
        insertSubmissions(submissions)
        upsertFetchTime(fetchTime)
    }

    @Query("DELETE FROM rating_changes WHERE handleKey = :handleKey")
    protected abstract suspend fun deleteRatingChanges(handleKey: String)

    @Upsert
    protected abstract suspend fun insertRatingChanges(changes: List<RatingChangeEntity>)

    @Query("DELETE FROM submissions WHERE handleKey = :handleKey")
    protected abstract suspend fun deleteSubmissions(handleKey: String)

    @Upsert
    protected abstract suspend fun insertSubmissions(submissions: List<SubmissionEntity>)

    @Upsert
    protected abstract suspend fun upsertFetchTime(fetchTime: FetchTimeEntity)
}
