package com.rkd.chatapi.message.domain.repository

import com.rkd.chatapi.conversation.domain.entity.Conversation
import com.rkd.chatapi.message.domain.entity.Message
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface MessageRepository : JpaRepository<Message, Long> {
    fun findByConversationOrderByCreatedAtDesc(conversation: Conversation, pageable: Pageable): List<Message>

    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation AND m.id < :cursor ORDER BY m.createdAt DESC")
    fun findByConversationAndCursorOrderByCreatedAtDesc(conversation: Conversation, cursor: Long, pageable: Pageable): List<Message>

    @Query("SELECT COUNT(m) FROM Message m WHERE m.conversation = :conversation AND m.id > :afterMessageId AND m.id < :beforeMessageId")
    fun countByConversationAndIdBetween(conversation: Conversation, afterMessageId: Long, beforeMessageId: Long): Long

    @Query("SELECT m FROM Message m WHERE m.conversation = :conversation AND m.id > :afterMessageId AND m.id < :beforeMessageId ORDER BY m.createdAt ASC")
    fun findByConversationAndIdBetweenOrderByCreatedAtAsc(conversation: Conversation, afterMessageId: Long, beforeMessageId: Long): List<Message>
}
