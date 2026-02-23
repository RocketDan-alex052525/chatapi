package com.rkd.chatapi.conversation.domain.entity

import com.rkd.chatapi.common.domain.entity.BaseTimeEntity
import com.rkd.chatapi.conversation.exception.ConversationAccessDeniedException
import com.rkd.chatapi.message.domain.entity.Message
import com.rkd.chatapi.user.domain.entity.User
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.OneToOne

@Entity
class Conversation(
    user: User,
    title: String
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    var user: User = user
        private set

    @Column(name = "title", nullable = false, length = 255)
    var title: String = title
        private set

    @Column(name = "summary", columnDefinition = "TEXT")
    var summary: String? = null
        private set

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_summarized_message_id")
    var lastSummarizedMessage: Message? = null
        private set

    @OneToMany(mappedBy = "conversation", cascade = [CascadeType.ALL], orphanRemoval = true)
    var messages: MutableList<Message> = mutableListOf()

    fun validateOwner(userId: Long) {
        if (user.id != userId) {
            throw ConversationAccessDeniedException()
        }
    }

    fun updateSummary(summary: String, lastSummarizedMessage: Message) {
        this.summary = summary
        this.lastSummarizedMessage = lastSummarizedMessage
    }
}
