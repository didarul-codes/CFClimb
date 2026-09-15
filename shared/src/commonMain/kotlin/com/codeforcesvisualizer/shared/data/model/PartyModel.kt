package com.codeforcesvisualizer.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PartyModel(
    @SerialName("participantType")
    val participantType: String? = null
)
