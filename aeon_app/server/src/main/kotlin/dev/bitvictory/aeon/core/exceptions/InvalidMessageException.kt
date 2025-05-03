package dev.bitvictory.aeon.core.exceptions

data class InvalidMessageException(override val message: String) : RuntimeException(message)