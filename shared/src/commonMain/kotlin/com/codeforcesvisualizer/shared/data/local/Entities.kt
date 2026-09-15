package com.codeforcesvisualizer.shared.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "contests")
data class ContestEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val type: String,
    val phase: String,
    val frozen: Boolean,
    val scheduled: Boolean,
    val durationSeconds: Int,
    val startTimeSeconds: Int,
    val relativeTimeSeconds: Int,
    val preparedBy: String?,
    val websiteUrl: String?,
    val description: String?,
    val difficulty: Int?,
    val kind: String?,
    val icpcRegion: String?,
    val country: String?,
    val season: String?,
)

/** Profiles are keyed by [handleKey], the lowercase handle, because Codeforces handles ignore case. */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val handleKey: String,
    val handle: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val country: String,
    val city: String,
    val organization: String,
    val contribution: Int,
    val rank: String,
    val rating: Int,
    val maxRank: String,
    val maxRating: Int,
    val lastOnlineTimeSeconds: Int,
    val registrationTimeSeconds: Int,
    val friendOfCount: Int,
    val avatar: String,
    val titlePhoto: String,
)

@Entity(
    tableName = "rating_changes",
    primaryKeys = ["handleKey", "contestId"],
    indices = [Index("handleKey")],
)
data class RatingChangeEntity(
    val handleKey: String,
    val contestId: Int,
    val contestName: String,
    val rank: Int,
    val ratingUpdateTimeSeconds: Long,
    val oldRating: Int,
    val newRating: Int,
)

@Entity(
    tableName = "submissions",
    primaryKeys = ["handleKey", "id"],
    indices = [Index("handleKey")],
)
data class SubmissionEntity(
    val handleKey: String,
    val id: Long,
    val creationTimeSeconds: Long,
    val participantType: String,
    val programmingLanguage: String,
    val verdict: String,
    val problemContestId: Int?,
    val problemsetName: String?,
    val problemIndex: String,
    val problemName: String,
    val problemRating: Int?,
    val problemTags: List<String>,
)

/**
 * When a cached collection was last fetched. Its presence tells "never loaded" apart from
 * "loaded, and empty" (a user with no rated contests).
 */
@Entity(tableName = "fetch_times")
data class FetchTimeEntity(
    @PrimaryKey val key: String,
    val fetchedAtEpochSeconds: Long,
)
