package dev.bitvictory.aeon.core.domain.entities.assistant

import com.aallam.openai.api.core.Status

data class RunState(
    val id: String,
    val status: Status,
    val error: String?
)
