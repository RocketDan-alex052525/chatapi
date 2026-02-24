package com.rkd.chatapi.chat.controller

import com.rkd.chatapi.common.annotation.LoginUser
import com.rkd.chatapi.chat.dto.request.ChatCompletionRequest
import com.rkd.chatapi.chat.dto.response.ChatCompletionResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@Tag(name = "Chat", description = "채팅 완성 API")
interface ChatCompletionApi {

    @Operation(
        summary = "채팅 완성",
        description = "메시지를 전송하고 AI 응답을 받습니다. 이전 대화 히스토리를 포함하여 컨텍스트를 유지합니다.",
        security = [SecurityRequirement(name = "cookieAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "성공"),
        ApiResponse(responseCode = "401", description = "인증 실패 — 액세스 토큰 없음 (JWT0002)"),
        ApiResponse(responseCode = "429", description = "Rate Limit 초과 — 분(RATE0001) / 시간(RATE0002) / 일(RATE0003)"),
    )
    @PostMapping("/completions")
    fun createCompletion(
        @RequestBody request: ChatCompletionRequest,
        @LoginUser userId: Long
    ): ResponseEntity<ChatCompletionResponse>

    @Operation(
        summary = "채팅 완성 (스트리밍)",
        description = "메시지를 전송하고 AI 응답을 SSE 스트림으로 받습니다.",
        security = [SecurityRequirement(name = "cookieAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "성공 (text/event-stream)"),
        ApiResponse(responseCode = "401", description = "인증 실패 — 액세스 토큰 없음 (JWT0002)"),
        ApiResponse(responseCode = "429", description = "Rate Limit 초과 — 분(RATE0001) / 시간(RATE0002) / 일(RATE0003)"),
    )
    @PostMapping("/completions/stream")
    fun createCompletionStream(
        @RequestBody request: ChatCompletionRequest,
        @LoginUser userId: Long
    ): SseEmitter
}
