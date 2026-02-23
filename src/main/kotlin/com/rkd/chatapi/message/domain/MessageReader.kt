package com.rkd.chatapi.message.domain

import com.rkd.chatapi.conversation.domain.entity.Conversation
import com.rkd.chatapi.message.domain.entity.Message
import com.rkd.chatapi.message.domain.repository.MessageRepository
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Component

@Component
class MessageReader(
    private val messageRepository: MessageRepository
) {
    fun findMessagesByConversation(conversation: Conversation, pageable: Pageable): List<Message> {
        return messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable)
    }

    fun findMessagesByConversationWithCursor(conversation: Conversation, cursor: Long, pageable: Pageable): List<Message> {
        return messageRepository.findByConversationAndCursorOrderByCreatedAtDesc(conversation, cursor, pageable)
    }

    fun countMessagesInRange(conversation: Conversation, afterMessageId: Long, beforeMessageId: Long): Long {
        return messageRepository.countByConversationAndIdBetween(conversation, afterMessageId, beforeMessageId)
    }

    fun findMessagesInRange(conversation: Conversation, afterMessageId: Long, beforeMessageId: Long): List<Message> {
        return messageRepository.findByConversationAndIdBetweenOrderByCreatedAtAsc(conversation, afterMessageId, beforeMessageId)
    }
}
