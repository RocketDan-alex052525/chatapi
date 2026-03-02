package com.rkd.chatapi.conversation.domain

import com.rkd.chatapi.conversation.domain.entity.Conversation
import com.rkd.chatapi.conversation.domain.repository.ConversationRepository
import com.rkd.chatapi.conversation.exception.ConversationNotExistException
import com.rkd.chatapi.user.domain.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageRequest
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ConversationReaderTest {

    @InjectMocks
    private lateinit var conversationReader: ConversationReader

    @Mock
    private lateinit var conversationRepository: ConversationRepository

    private val user = User(apiKey = "key", apiKeyEnc = "enc")
    private val conversation = Conversation(user = user, title = "test")

    @Test
    fun `findConversationById returns conversation when found`() {
        // given
        whenever(conversationRepository.findById(1L)).thenReturn(Optional.of(conversation))

        // when
        val result = conversationReader.findConversationById(1L)

        // then
        assertThat(result).isEqualTo(conversation)
    }

    @Test
    fun `findConversationById throws ConversationNotExistException when not found`() {
        // given
        whenever(conversationRepository.findById(1L)).thenReturn(Optional.empty())

        // when & then
        assertThrows<ConversationNotExistException> { conversationReader.findConversationById(1L) }
    }

    @Test
    fun `findConversationsByUserId returns conversation list`() {
        // given
        val pageable = PageRequest.of(0, 10)
        whenever(conversationRepository.findByUserIdOrderByCreatedAtDesc(1L, pageable))
            .thenReturn(listOf(conversation))

        // when
        val result = conversationReader.findConversationsByUserId(1L, pageable)

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(conversation)
    }

    @Test
    fun `findConversationsByUserIdWithCursor returns conversation list`() {
        // given
        val pageable = PageRequest.of(0, 10)
        whenever(conversationRepository.findByUserAndCursorOrderByCreatedAtDesc(1L, 5L, pageable))
            .thenReturn(listOf(conversation))

        // when
        val result = conversationReader.findConversationsByUserIdWithCursor(1L, 5L, pageable)

        // then
        assertThat(result).hasSize(1)
        assertThat(result[0]).isEqualTo(conversation)
    }
}
