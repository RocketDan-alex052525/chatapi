package com.rkd.chatapi.conversation.service

import com.rkd.chatapi.conversation.domain.ConversationReader
import com.rkd.chatapi.conversation.domain.entity.Conversation
import com.rkd.chatapi.conversation.dto.response.ConversationInfoResponse
import com.rkd.chatapi.conversation.dto.response.ConversationListResponse
import com.rkd.chatapi.message.domain.MessageReader
import com.rkd.chatapi.message.domain.entity.Message
import com.rkd.chatapi.message.dto.response.MessageInfoResponse
import com.rkd.chatapi.message.dto.response.MessageListResponse
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Service
class ConversationInfoService(
    private val messageReader: MessageReader,
    private val conversationReader: ConversationReader
) {
    fun getConversations(userId: Long, cursor: Long?, size: Int): ConversationListResponse {
        val conversations = fetchConversations(userId, cursor, size)
        val hasNext = conversations.size > size
        val result = conversations.take(size)

        return ConversationListResponse(
            conversations = result.map { it.toInfoResponse() },
            nextCursor = if (hasNext) result.last().id else null,
            hasNext = hasNext
        )
    }

    fun getConversationWithMessages(userId: Long, conversationId: Long, cursor: Long?, size: Int): MessageListResponse {
        val conversation = conversationReader.findConversationById(conversationId)
        conversation.validateOwner(userId)

        val messages = fetchMessages(conversation, cursor, size)
        val hasNext = messages.size > size
        val result = messages.take(size)

        return MessageListResponse(
            messages = result.map { it.toInfoResponse() },
            nextCursor = if (hasNext) result.last().id else null,
            hasNext = hasNext
        )
    }

    private fun fetchConversations(userId: Long, cursor: Long?, size: Int): List<Conversation> {
        val pageable = PageRequest.of(0, size + 1)
        return cursor?.let {
            conversationReader.findConversationsByUserIdWithCursor(userId, it, pageable)
        } ?: conversationReader.findConversationsByUserId(userId, pageable)
    }

    private fun fetchMessages(conversation: Conversation, cursor: Long?, size: Int): List<Message> {
        val pageable = PageRequest.of(0, size + 1)
        return cursor?.let {
            messageReader.findMessagesByConversationWithCursor(conversation, it, pageable)
        } ?: messageReader.findMessagesByConversation(conversation, pageable)
    }

    private fun Conversation.toInfoResponse() = ConversationInfoResponse(
        conversationId = id!!,
        title = title
    )

    private fun Message.toInfoResponse() = MessageInfoResponse(
        role = role.name,
        content = content
    )
}
