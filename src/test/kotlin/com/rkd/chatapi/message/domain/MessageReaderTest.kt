package com.rkd.chatapi.message.domain

import com.rkd.chatapi.conversation.domain.entity.Conversation
import com.rkd.chatapi.message.domain.entity.Message
import com.rkd.chatapi.message.domain.repository.MessageRepository
import com.rkd.chatapi.user.domain.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest

@ExtendWith(MockitoExtension::class)
class MessageReaderTest {

    @InjectMocks
    private lateinit var messageReader: MessageReader

    @Mock
    private lateinit var messageRepository: MessageRepository

    private val user = User(apiKey = "key", apiKeyEnc = "enc")
    private val conversation = Conversation(user = user, title = "test")
    private val message = Message(conversation = conversation, role = MessageRole.USER, content = "hello")

    @Test
    fun `findMessagesByConversation returns message list`() {
        // given
        val pageable = PageRequest.of(0, 10)
        whenever(messageRepository.findByConversationOrderByCreatedAtDesc(conversation, pageable))
            .thenReturn(listOf(message))

        // when
        val result = messageReader.findMessagesByConversation(conversation, pageable)

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(message)
    }

    @Test
    fun `findMessagesByConversationWithCursor returns message list`() {
        // given
        val pageable = PageRequest.of(0, 10)
        whenever(messageRepository.findByConversationAndCursorOrderByCreatedAtDesc(conversation, 5L, pageable))
            .thenReturn(listOf(message))

        // when
        val result = messageReader.findMessagesByConversationWithCursor(conversation, 5L, pageable)

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(message)
    }

    @Test
    fun `countMessagesInRange returns count`() {
        // given
        whenever(messageRepository.countByConversationAndIdBetween(conversation, 0L, 10L))
            .thenReturn(5L)

        // when
        val result = messageReader.countMessagesInRange(conversation, 0L, 10L)

        // then
        assertThat(result).isEqualTo(5L)
    }

    @Test
    fun `findMessagesInRange returns message list`() {
        // given
        whenever(messageRepository.findByConversationAndIdBetweenOrderByCreatedAtAsc(conversation, 0L, 10L))
            .thenReturn(listOf(message))

        // when
        val result = messageReader.findMessagesInRange(conversation, 0L, 10L)

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(message)
    }
}
