package com.codeforcesvisualizer.shared.data.model

import com.codeforcesvisualizer.shared.domain.entity.Problem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** problemset.problems; the problemStatistics part of the result isn't used. */
@Serializable
data class ProblemsetResponseModel(
    @SerialName("status")
    override val statusModel: StatusModel,
    @SerialName("result")
    val result: ProblemsetResultModel? = null,
    @SerialName("comment")
    override val comment: String? = null
) : BaseResponseModel {
    fun toEntity(): List<Problem> {
        return result?.problems?.map { it.toEntity() } ?: emptyList()
    }
}

@Serializable
data class ProblemsetResultModel(
    @SerialName("problems")
    val problems: List<ProblemModel> = emptyList()
)
