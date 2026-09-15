package com.codeforcesvisualizer.shared.data.model

import com.codeforcesvisualizer.shared.domain.entity.ParticipantType
import com.codeforcesvisualizer.shared.domain.entity.UserStatus
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserStatusModel(
    @SerialName("id")
    val id: Long,

    @SerialName("creationTimeSeconds")
    val creationTimeSeconds: Long,

    @SerialName("author")
    val author: PartyModel? = null,

    @SerialName("programmingLanguage")
    val programmingLanguage: String,

    /** Absent while the submission is still in the judging queue. */
    @SerialName("verdict")
    val verdict: String? = null,

    @SerialName("problem")
    val problemModel: ProblemModel
) {
    fun toEntity(): UserStatus {
        return UserStatus(
            id = id,
            creationTimeSeconds = creationTimeSeconds,
            participantType = author?.participantType.toParticipantType(),
            programmingLanguage = programmingLanguage,
            verdict = verdict ?: UserStatus.VERDICT_TESTING,
            problem = problemModel.toEntity()
        )
    }
}

private fun String?.toParticipantType(): ParticipantType {
    return ParticipantType.entries.firstOrNull { it.name == this } ?: ParticipantType.UNKNOWN
}
