package com.codeforcesvisualizer.shared.data.model

import com.codeforcesvisualizer.shared.domain.entity.Problem
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProblemModel(
    @SerialName("contestId")
    val contestId: Int? = null,

    @SerialName("problemsetName")
    val problemsetName: String? = null,

    @SerialName("index")
    val index: String,

    @SerialName("rating")
    val rating: Int? = null,

    @SerialName("tags")
    val tags: List<String> = emptyList(),

    @SerialName("name")
    val name: String
) {
    fun toEntity(): Problem {
        return Problem(
            contestId = contestId,
            problemsetName = problemsetName,
            index = index,
            name = name,
            rating = rating,
            tags = tags
        )
    }
}
