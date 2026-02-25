package com.rkd.chatapi.chat.controller

import com.rkd.chatapi.common.annotation.LoginUser
import com.rkd.chatapi.chat.adapter.OpenAiChatAdapter.Companion.STREAM_DONE_MARKER
import com.rkd.chatapi.chat.dto.request.ChatCompletionRequest
import com.rkd.chatapi.chat.dto.response.ChatCompletionResponse
import com.rkd.chatapi.chat.service.ChatCompletionService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/chat")
class ChatCompletionController(
    private val chatCompletionService: ChatCompletionService,
    @Value("\${openai.stream-timeout-ms}") private val streamTimeoutMs: Long
) : ChatCompletionApi {
    override fun createCompletion(
        @RequestBody request: ChatCompletionRequest,
        @LoginUser userId: Long
    ): ResponseEntity<ChatCompletionResponse> {
        val chatCompletionResponse = chatCompletionService.completeChat(userId, request)
        return ResponseEntity.ok(chatCompletionResponse)
    }

    @PostMapping("/completions/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    override fun createCompletionStream(
        @RequestBody request: ChatCompletionRequest,
        @LoginUser userId: Long
    ): SseEmitter {
        val emitter = SseEmitter(streamTimeoutMs)

        chatCompletionService.completeChatStream(userId, request)
            .doOnNext { chunk ->
                emitter.send(SseEmitter.event().data(chunk, MediaType.APPLICATION_JSON))
            }
            .doOnComplete {
                emitter.send(SseEmitter.event().data(STREAM_DONE_MARKER))
                emitter.complete()
            }
            .doOnError { error ->
                emitter.completeWithError(error)
            }
            .subscribe()

        return emitter
    }
}