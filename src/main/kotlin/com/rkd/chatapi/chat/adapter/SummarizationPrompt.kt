package com.rkd.chatapi.chat.adapter

enum class SummarizationPrompt(private val template: String) {

    INITIAL(
        """
        You are a conversation summarizer. Below are messages from a conversation.

        Please create a concise but comprehensive summary preserving key context, decisions, and any important details.
        The summary must be within {max_length} characters.
        Respond with only the summary text.
        """.trimIndent()
    ),

    INCREMENTAL(
        """
        You are a conversation summarizer. Below is the existing summary of an earlier part of the conversation, followed by new messages that have not yet been summarized.

        Existing summary:
        {existing_summary}

        Please update the summary to incorporate the new messages below. Keep the summary concise but comprehensive, preserving key context, decisions, and any important details.
        The summary must be within {max_length} characters.
        Respond with only the updated summary text.
        """.trimIndent()
    );

    fun render(vararg params: Pair<String, String>): String =
        params.fold(template) { acc, (key, value) -> acc.replace("{$key}", value) }
}
