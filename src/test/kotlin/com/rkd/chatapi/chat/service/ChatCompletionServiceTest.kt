package com.rkd.chatapi.chat.service

import com.rkd.chatapi.chat.dto.OpenAiChatMessage
import com.rkd.chatapi.conversation.domain.ConversationReader
import com.rkd.chatapi.conversation.domain.ConversationWriter
import com.rkd.chatapi.conversation.domain.entity.Conversation
import com.rkd.chatapi.message.domain.MessageReader
import com.rkd.chatapi.message.domain.MessageRole
import com.rkd.chatapi.message.domain.MessageWriter
import com.rkd.chatapi.message.domain.entity.Message
import com.rkd.chatapi.chat.dto.request.ChatCompletionRequest
import com.rkd.chatapi.chat.adapter.OpenAiChatAdapter
import com.rkd.chatapi.user.domain.entity.User
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argThat
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.Pageable
import reactor.core.publisher.Flux

@ExtendWith(MockitoExtension::class)
class ChatCompletionServiceTest {

    private lateinit var chatCompletionService: ChatCompletionService

    @Mock
    private lateinit var conversationReader: ConversationReader

    @Mock
    private lateinit var conversationWriter: ConversationWriter

    @Mock
    private lateinit var messageReader: MessageReader

    @Mock
    private lateinit var messageWriter: MessageWriter

    @Mock
    private lateinit var openAiChatAdapter: OpenAiChatAdapter

    @BeforeEach
    fun setUp() {
        chatCompletionService = ChatCompletionService(
            conversationReader = conversationReader,
            conversationWriter = conversationWriter,
            messageReader = messageReader,
            messageWriter = messageWriter,
            openAiChatAdapter = openAiChatAdapter,
            historyLimit = 10,
            summaryTriggerCount = 5
        )
    }

    @Test
    fun `completeChat saves user and assistant messages and returns response`() {
        val conversation = Conversation(
            user = User(apiKey = "hashed-key", apiKeyEnc = "enc-key"),
            title = "hello"
        ).apply { id = 1L }
        val request = ChatCompletionRequest(conversationId = 1L, content = "hi")

        whenever(conversationReader.findConversationById(1L)).thenReturn(conversation)
        whenever(messageReader.findMessagesByConversation(any(), any<Pageable>()))
            .thenReturn(emptyList())
        whenever(openAiChatAdapter.completeChat(any(), any<List<OpenAiChatMessage>>())).thenReturn("answer")

        var saveCount = 0
        whenever(messageWriter.save(any<Message>())).thenAnswer { invocation ->
            saveCount += 1
            (invocation.arguments[0] as Message).apply {
                id = if (saveCount == 1) 100L else 200L
            }
        }

        val response = chatCompletionService.completeChat(1L, request)

        assertThat(response.messageId).isEqualTo(200L)
        assertThat(response.answer).isEqualTo("answer")
    }

    @Test
    fun `completeChatStream saves assistant message after stream completes`() {
        val conversation = Conversation(
            user = User(apiKey = "hashed-key", apiKeyEnc = "enc-key"),
            title = "hello"
        ).apply { id = 1L }
        val request = ChatCompletionRequest(conversationId = 1L, content = "hi")

        whenever(conversationReader.findConversationById(1L)).thenReturn(conversation)
        whenever(messageReader.findMessagesByConversation(any(), any<Pageable>()))
            .thenReturn(emptyList())
        whenever(openAiChatAdapter.completeChatStream(any(), any<List<OpenAiChatMessage>>()))
            .thenReturn(Flux.just("Hello", " world"))
        whenever(messageWriter.save(any<Message>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Message).apply { id = 100L }
        }

        val flux = chatCompletionService.completeChatStream(1L, request)
        flux.blockLast()

        verify(messageWriter).save(argThat<Message> { role == MessageRole.USER })
        verify(messageWriter).save(argThat<Message> { role == MessageRole.ASSISTANT && content == "Hello world" })
    }

    @Test
    fun `prepareChat includes summary as system message when conversation has summary`() {
        val conversation = Conversation(
            user = User(apiKey = "hashed-key", apiKeyEnc = "enc-key"),
            title = "hello"
        ).apply {
            id = 1L
            val msg5 = Message(conversation = this, role = MessageRole.USER, content = "").apply { id = 5L }
            updateSummary("User asked about Seoul population", msg5)
        }
        val request = ChatCompletionRequest(conversationId = 1L, content = "hi")

        whenever(conversationReader.findConversationById(1L)).thenReturn(conversation)
        whenever(messageReader.findMessagesByConversation(any(), any<Pageable>()))
            .thenReturn(emptyList())
        whenever(openAiChatAdapter.completeChat(any(), any<List<OpenAiChatMessage>>()))
            .thenReturn("answer")
        whenever(messageWriter.save(any<Message>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Message).apply { id = 100L }
        }

        chatCompletionService.completeChat(1L, request)

        verify(openAiChatAdapter).completeChat(
            any(),
            argThat<List<OpenAiChatMessage>> {
                size == 2 &&
                this[0].role == "system" &&
                this[0].content.contains("User asked about Seoul population") &&
                this[1].role == "user" &&
                this[1].content == "hi"
            }
        )
    }

    @Test
    fun `trySummarize triggers when unsummarized count meets threshold`() {
        val conversation = Conversation(
            user = User(apiKey = "hashed-key", apiKeyEnc = "enc-key"),
            title = "hello"
        ).apply { id = 1L }
        val request = ChatCompletionRequest(conversationId = 1L, content = "hi")

        whenever(conversationReader.findConversationById(1L)).thenReturn(conversation)

        val recentMessages = (11L..20L).map { msgId ->
            Message(conversation = conversation, role = MessageRole.ASSISTANT, content = "msg$msgId")
                .apply { id = msgId }
        }.reversed()

        whenever(messageReader.findMessagesByConversation(any(), any<Pageable>()))
            .thenReturn(recentMessages)
        whenever(messageReader.countMessagesInRange(conversation, 0L, 11L))
            .thenReturn(10L)

        val oldMessages = (1L..10L).map { msgId ->
            Message(conversation = conversation, role = MessageRole.USER, content = "old-msg$msgId")
                .apply { id = msgId }
        }
        whenever(messageReader.findMessagesInRange(conversation, 0L, 11L))
            .thenReturn(oldMessages)

        whenever(openAiChatAdapter.completeChat(any(), any<List<OpenAiChatMessage>>()))
            .thenReturn("answer")
        whenever(openAiChatAdapter.summarize(any(), anyOrNull(), any<List<OpenAiChatMessage>>()))
            .thenReturn("new summary")

        var saveCount = 0
        whenever(messageWriter.save(any<Message>())).thenAnswer { invocation ->
            saveCount += 1
            (invocation.arguments[0] as Message).apply {
                id = if (saveCount == 1) 100L else 200L
            }
        }

        chatCompletionService.completeChat(1L, request)

        verify(openAiChatAdapter).summarize(any(), anyOrNull(), any<List<OpenAiChatMessage>>())
        verify(conversationWriter).save(argThat<Conversation> {
            summary == "new summary" && lastSummarizedMessage?.id == 10L
        })
    }

    @Test
    fun `trySummarize does not trigger when unsummarized count is below threshold`() {
        val conversation = Conversation(
            user = User(apiKey = "hashed-key", apiKeyEnc = "enc-key"),
            title = "hello"
        ).apply { id = 1L }
        val request = ChatCompletionRequest(conversationId = 1L, content = "hi")

        whenever(conversationReader.findConversationById(1L)).thenReturn(conversation)

        val recentMessages = listOf(
            Message(conversation = conversation, role = MessageRole.USER, content = "msg1")
                .apply { id = 5L },
            Message(conversation = conversation, role = MessageRole.ASSISTANT, content = "msg2")
                .apply { id = 4L }
        )
        whenever(messageReader.findMessagesByConversation(any(), any<Pageable>()))
            .thenReturn(recentMessages)
        whenever(messageReader.countMessagesInRange(conversation, 0L, 4L))
            .thenReturn(2L)

        whenever(openAiChatAdapter.completeChat(any(), any<List<OpenAiChatMessage>>()))
            .thenReturn("answer")
        whenever(messageWriter.save(any<Message>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Message).apply { id = 100L }
        }

        chatCompletionService.completeChat(1L, request)

        verify(conversationWriter, never()).save(any())
    }

    @Test
    fun `trySummarize performs incremental summarization with existing summary`() {
        val conversation = Conversation(
            user = User(apiKey = "hashed-key", apiKeyEnc = "enc-key"),
            title = "hello"
        ).apply {
            id = 1L
            val msg10 = Message(conversation = this, role = MessageRole.USER, content = "").apply { id = 10L }
            updateSummary("old summary", msg10)
        }
        val request = ChatCompletionRequest(conversationId = 1L, content = "hi")

        whenever(conversationReader.findConversationById(1L)).thenReturn(conversation)

        val recentMessages = (21L..30L).map { msgId ->
            Message(conversation = conversation, role = MessageRole.USER, content = "msg$msgId")
                .apply { id = msgId }
        }.reversed()
        whenever(messageReader.findMessagesByConversation(any(), any<Pageable>()))
            .thenReturn(recentMessages)

        whenever(messageReader.countMessagesInRange(conversation, 10L, 21L))
            .thenReturn(10L)

        val messagesToSummarize = (11L..20L).map { msgId ->
            Message(conversation = conversation, role = MessageRole.USER, content = "msg$msgId")
                .apply { id = msgId }
        }
        whenever(messageReader.findMessagesInRange(conversation, 10L, 21L))
            .thenReturn(messagesToSummarize)

        whenever(openAiChatAdapter.completeChat(any(), any<List<OpenAiChatMessage>>()))
            .thenReturn("answer")
        whenever(openAiChatAdapter.summarize(any(), anyOrNull(), any<List<OpenAiChatMessage>>()))
            .thenReturn("updated summary")
        whenever(messageWriter.save(any<Message>())).thenAnswer { invocation ->
            (invocation.arguments[0] as Message).apply { id = 100L }
        }

        chatCompletionService.completeChat(1L, request)

        verify(openAiChatAdapter).summarize(any(), anyOrNull(), any<List<OpenAiChatMessage>>())
        verify(conversationWriter).save(argThat<Conversation> {
            summary == "updated summary" && lastSummarizedMessage?.id == 20L
        })
    }
}
