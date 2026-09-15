package com.codeforcesvisualizer.shared.domain.entity

data class UserStatus(
    val id: Long,
    val creationTimeSeconds: Long,
    val participantType: ParticipantType,
    val programmingLanguage: String,
    /** Codeforces verdict such as "OK" or "WRONG_ANSWER"; [VERDICT_TESTING] while still judging. */
    val verdict: String,
    val problem: Problem
) {
    val isAccepted: Boolean get() = verdict == VERDICT_OK

    companion object {
        const val VERDICT_OK = "OK"
        const val VERDICT_TESTING = "TESTING"
    }
}

enum class ParticipantType {
    CONTESTANT,
    PRACTICE,
    VIRTUAL,
    MANAGER,
    OUT_OF_COMPETITION,
    UNKNOWN
}
