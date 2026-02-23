package com.rkd.chatapi.message.domain

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM;

    fun toOpenAiRole(): String = name.lowercase()
}