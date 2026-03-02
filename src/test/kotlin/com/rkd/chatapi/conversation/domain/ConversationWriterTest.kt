package com.rkd.chatapi.conversation.domain

import com.rkd.chatapi.conversation.domain.entity.Conversation
import com.rkd.chatapi.conversation.domain.repository.ConversationRepository
import com.rkd.chatapi.user.domain.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ConversationWriterTest {

    @InjectMocks
    private lateinit var conversationWriter: ConversationWriter

    @Mock
    private lateinit var conversationRepository: ConversationRepository

    private val user = User(apiKey = "key", apiKeyEnc = "enc")
    private val conversation = Conversation(user = user, title = "test")

    @Test
    fun `save returns saved conversation`() {
        // given
        whenever(conversationRepository.save(conversation)).thenReturn(conversation)

        // when
        val result = conversationWriter.save(conversation)

        // then
        assertThat(result).isEqualTo(conversation)
    }

    @Test
    fun `delete calls repository delete`() {
        // when
        conversationWriter.delete(conversation)

        // then
        verify(conversationRepository).delete(conversation)
    }
}
