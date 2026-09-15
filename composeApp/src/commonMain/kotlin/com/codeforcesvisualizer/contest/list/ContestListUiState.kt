package com.codeforcesvisualizer.contest.list

import com.codeforcesvisualizer.shared.domain.entity.Contest

data class ContestListUiState(
    val refreshing: Boolean = false,
    val loading: Boolean = false,
    val contestList: List<Contest> = emptyList(),
    val groups: ContestGroups = ContestGroups(),
    val nowEpochSeconds: Long = 0,
    /** Shown instead of the list when nothing is saved and loading failed. */
    val userMessage: String = "",
    /** A refresh failed while saved contests are on screen. */
    val refreshError: String = "",
)
