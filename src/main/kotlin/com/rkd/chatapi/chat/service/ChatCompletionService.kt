package com.rkd.chatapi.chat.service

import com.rkd.chatapi.conversation.domain.ConversationReader
import com.rkd.chatapi.conversation.domain.ConversationWriter
import com.rkd.chatapi.message.domain.MessageReader
import com.rkd.chatapi.message.domain.MessageWriter
import com.rkd.chatapi.message.domain.MessageRole
import com.rkd.chatapi.message.domain.entity.Message
import com.rkd.chatapi.chat.dto.request.ChatCompletionRequest
import com.rkd.chatapi.chat.dto.response.ChatCompletionResponse
import com.rkd.chatapi.chat.dto.response.ChatStreamChunk
import com.rkd.chatapi.chat.adapter.OpenAiChatAdapter
import com.rkd.chatapi.chat.dto.OpenAiChatMessage
import com.rkd.chatapi.conversation.domain.entity.Conversation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux

@Service
class ChatCompletionService(
    private val conversationReader: ConversationReader,
    private val conversationWriter: ConversationWriter,
    private val messageReader: MessageReader,
    private val messageWriter: MessageWriter,
    private val openAiChatAdapter: OpenAiChatAdapter,
    @Value("\${openai.history-limit}") private val historyLimit: Int,
    @Value("\${openai.summary-trigger-count}") private val summaryTriggerCount: Int
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun completeChat(userId: Long, request: ChatCompletionRequest): ChatCompletionResponse {
        val (conversation, allMessages) = prepareChat(request)

        val answer = openAiChatAdapter.completeChat(userId, allMessages)
        val savedAssistant = saveAssistantMessage(conversation, answer)
        trySummarize(userId, conversation)

        return ChatCompletionResponse(
            messageId = savedAssistant.id!!,
            answer = answer
        )
    }

    fun completeChatStream(userId: Long, request: ChatCompletionRequest): Flux<ChatStreamChunk> {
        val (conversation, allMessages) = prepareChat(request)
        val contentBuffer = StringBuilder()

        return openAiChatAdapter.completeChatStream(userId, allMessages)
            .doOnNext { chunk -> contentBuffer.append(chunk) }
            .map { chunk -> ChatStreamChunk(content = chunk) }
            .doOnComplete {
                saveAssistantMessage(conversation, contentBuffer.toString())
                trySummarize(userId, conversation)
            }
    }

    private fun prepareChat(request: ChatCompletionRequest): Pair<Conversation, List<OpenAiChatMessage>> {
        val conversation = conversationReader.findConversationById(request.conversationId)
        val previousMessages = getPreviousMessages(conversation)
        saveUserMessage(conversation, request.content)

        val messages = mutableListOf<OpenAiChatMessage>()

        conversation.summary?.let { summary ->
            messages.add(
                OpenAiChatMessage(
                    role = MessageRole.SYSTEM.toOpenAiRole(),
                    content = "Summary of earlier conversation:\n$summary"
                )
            )
        }

        messages.addAll(previousMessages)
        messages.add(OpenAiChatMessage(role = MessageRole.USER.toOpenAiRole(), content = request.content))

        return Pair(conversation, messages)
    }

    private fun trySummarize(userId: Long, conversation: Conversation) {
        try {
            val messagesToSummarize = findMessagesToSummarize(conversation) ?: return

            val openAiMessages = messagesToSummarize.map {
                OpenAiChatMessage(role = it.role.toOpenAiRole(), content = it.content)
            }
            val newSummary = openAiChatAdapter.summarize(userId, conversation.summary, openAiMessages)

            conversation.updateSummary(newSummary, messagesToSummarize.last())
            conversationWriter.save(conversation)
        } catch (e: Exception) {
            log.warn("Summarization failed for conversation {}: {}", conversation.id, e.message)
        }
    }

    private fun findMessagesToSummarize(conversation: Conversation): List<Message>? {
        val (afterId, boundaryId) = calculateSummarizationRange(conversation) ?: return null
        return messageReader.findMessagesInRange(conversation, afterId, boundaryId)
    }

    private fun calculateSummarizationRange(conversation: Conversation): Pair<Long, Long>? {
        val recentMessages = messageReader.findMessagesByConversation(
            conversation,
            PageRequest.of(0, historyLimit)
        )
        if (recentMessages.isEmpty()) return null

        val boundaryId = recentMessages.last().id!!
        val afterId = conversation.lastSummarizedMessage?.id ?: 0L

        val unsummarizedCount = messageReader.countMessagesInRange(conversation, afterId, boundaryId)
        if (unsummarizedCount < summaryTriggerCount) return null

        return Pair(afterId, boundaryId)
    }

    private fun saveUserMessage(conversation: Conversation, content: String) {
        val userMessage = Message(
            conversation = conversation,
            role = MessageRole.USER,
            content = content
        )
        messageWriter.save(userMessage)
    }

    private fun saveAssistantMessage(conversation: Conversation, content: String): Message {
        val assistantMessage = Message(
            conversation = conversation,
            role = MessageRole.ASSISTANT,
            content = content
        )
        return messageWriter.save(assistantMessage)
    }

    private fun getPreviousMessages(conversation: Conversation): List<OpenAiChatMessage> {
        return messageReader
            .findMessagesByConversation(conversation, PageRequest.of(0, historyLimit))
            .reversed()
            .map { OpenAiChatMessage(role = it.role.toOpenAiRole(), content = it.content) }
    }
}
