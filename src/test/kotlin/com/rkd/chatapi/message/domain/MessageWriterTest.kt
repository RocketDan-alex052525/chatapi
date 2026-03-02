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

@ExtendWith(MockitoExtension::class)
class MessageWriterTest {

    @InjectMocks
    private lateinit var messageWriter: MessageWriter

    @Mock
    private lateinit var messageRepository: MessageRepository

    private val user = User(apiKey = "key", apiKeyEnc = "enc")
    private val conversation = Conversation(user = user, title = "test")
    private val message = Message(conversation = conversation, role = MessageRole.USER, content = "hello")

    @Test
    fun `save returns saved message`() {
        // given
        whenever(messageRepository.save(message)).thenReturn(message)

        // when
        val result = messageWriter.save(message)

        // then
        assertThat(result).isEqualTo(message)
    }
}
