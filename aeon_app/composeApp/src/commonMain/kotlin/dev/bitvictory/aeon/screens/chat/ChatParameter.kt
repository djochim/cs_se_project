package dev.bitvictory.aeon.screens.chat

import kotlinx.serialization.Serializable

@Serializable
data class ChatParameter(val query: String, val images: List<ByteArray>)
