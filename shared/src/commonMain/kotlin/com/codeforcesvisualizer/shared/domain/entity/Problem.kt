package com.codeforcesvisualizer.shared.domain.entity

data class Problem(
    /** Absent for problems that only exist in a problemset such as acmsguru. */
    val contestId: Int?,
    val problemsetName: String?,
    val index: String,
    val name: String,
    /** Difficulty rating (800–3500); null for unrated problems. */
    val rating: Int?,
    val tags: List<String>
) {
    /** Identifies a problem across submissions, e.g. "1401-F". */
    val key: String get() = "${contestId ?: problemsetName.orEmpty()}-$index"

    /** Short label as shown on Codeforces, e.g. "1401F". */
    val label: String get() = "${contestId ?: ""}$index"
}
