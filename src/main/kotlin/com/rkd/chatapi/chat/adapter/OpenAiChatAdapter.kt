package com.rkd.chatapi.chat.adapter

import com.rkd.chatapi.chat.dto.OpenAiChatMessage
import com.rkd.chatapi.chat.dto.request.OpenAiChatCompletionRequest
import com.rkd.chatapi.chat.dto.response.OpenAiChatCompletionResponse
import com.rkd.chatapi.chat.dto.response.OpenAiStreamChatCompletionResponse
import com.rkd.chatapi.common.security.ApiKeyEncryptor
import com.rkd.chatapi.message.domain.MessageRole
import com.rkd.chatapi.user.domain.UserReader
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Flux

@Component
class OpenAiChatAdapter(
    private val userReader: UserReader,
    private val apiKeyEncryptor: ApiKeyEncryptor,
    @Value("\${openai.base-url}") private val baseUrl: String,
    @Value("\${openai.model}") private val model: String,
    @Value("\${openai.connect-timeout-ms}") private val connectTimeoutMs: Long,
    @Value("\${openai.read-timeout-ms}") private val readTimeoutMs: Long,
    @Value("\${openai.summary-max-length}") private val summaryMaxLength: Int
) {
    companion object {
        const val STREAM_DONE_MARKER = "[DONE]"
    }

    private val objectMapper = jacksonObjectMapper()

    fun completeChat(userId: Long, messages: List<OpenAiChatMessage>): String {
        val apiKey = decryptApiKey(userId)

        val requestFactory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(connectTimeoutMs.toInt())
            setReadTimeout(readTimeoutMs.toInt())
        }

        val client = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .requestFactory(requestFactory)
            .build()

        val request = OpenAiChatCompletionRequest(
            model = model,
            messages = messages
        )

        val response = client.post()
            .uri("/chat/completions")
            .body(request)
            .retrieve()
            .body(OpenAiChatCompletionResponse::class.java)
            ?: throw IllegalStateException("OpenAI response is null")

        val message = response.choices.firstOrNull()?.message?.content
            ?: throw IllegalStateException("OpenAI response has no message")
        return message
    }

    fun completeChatStream(userId: Long, messages: List<OpenAiChatMessage>): Flux<String> {
        val apiKey = decryptApiKey(userId)

        val webClient = WebClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer $apiKey")
            .build()

        val request = OpenAiChatCompletionRequest(
            model = model,
            messages = messages,
            stream = true
        )

        return webClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(request)
            .retrieve()
            .bodyToFlux(String::class.java)
            .filter { it.isNotBlank() && it != STREAM_DONE_MARKER }
            .map { data ->
                val chunk = objectMapper.readValue(data, OpenAiStreamChatCompletionResponse::class.java)
                chunk.choices.firstOrNull()?.delta?.content ?: ""
            }
            .filter { it.isNotEmpty() }
    }

    fun summarize(userId: Long, existingSummary: String?, messages: List<OpenAiChatMessage>): String {
        val formattedMessages = messages.joinToString("\n") { "${it.role}: ${it.content}" }

        val chatMessages = listOf(
            OpenAiChatMessage(
                role = MessageRole.SYSTEM.toOpenAiRole(),
                content = buildSummarizationPrompt(existingSummary)
            ),
            OpenAiChatMessage(
                role = MessageRole.USER.toOpenAiRole(),
                content = formattedMessages
            )
        )

        return completeChat(userId, chatMessages)
    }

    private fun buildSummarizationPrompt(existingSummary: String?): String {
        return if (existingSummary != null) {
            SummarizationPrompt.INCREMENTAL.render(
                "existing_summary" to existingSummary,
                "max_length" to summaryMaxLength.toString()
            )
        } else {
            SummarizationPrompt.INITIAL.render(
                "max_length" to summaryMaxLength.toString()
            )
        }
    }

    private fun decryptApiKey(userId: Long): String {
        val user = userReader.findUserById(userId)
        val encrypted = user.apiKeyEnc ?: throw IllegalStateException("Encrypted API key missing")
        return apiKeyEncryptor.decrypt(encrypted)
    }
}
