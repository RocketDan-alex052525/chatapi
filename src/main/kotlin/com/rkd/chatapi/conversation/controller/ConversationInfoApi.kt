 package com.rkd.chatapi.conversation.controller

import com.rkd.chatapi.common.annotation.LoginUser
import com.rkd.chatapi.conversation.dto.response.ConversationListResponse
import com.rkd.chatapi.message.dto.response.MessageListResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "Conversation", description = "대화 조회 API")
interface ConversationInfoApi {

    @Operation(
        summary = "대화 목록 조회",
        description = "로그인한 사용자의 대화 목록을 커서 기반 페이지네이션으로 조회합니다.",
        security = [SecurityRequirement(name = "cookieAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "성공"),
        ApiResponse(responseCode = "401", description = "인증 실패 — 액세스 토큰 없음 (JWT0002)"),
    )
    @GetMapping
    fun getConversations(
        @LoginUser userId: Long,
        @Parameter(description = "커서 ID (이전 응답의 nextCursor 값)", required = false)
        @RequestParam(required = false) cursor: Long?,
        @Parameter(description = "조회 개수 (기본값: 5)", required = false)
        @RequestParam(defaultValue = "5") size: Int
    ): ResponseEntity<ConversationListResponse>

    @Operation(
        summary = "대화 메시지 목록 조회",
        description = "특정 대화의 메시지 목록을 커서 기반 페이지네이션으로 조회합니다.",
        security = [SecurityRequirement(name = "cookieAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "성공"),
        ApiResponse(responseCode = "400", description = "대화를 찾을 수 없음 (CONVERSATION0001)"),
        ApiResponse(responseCode = "401", description = "인증 실패 — 액세스 토큰 없음 (JWT0002)"),
        ApiResponse(responseCode = "403", description = "대화 접근 권한 없음 (CONVERSATION0002)"),
    )
    @GetMapping("/{conversationId}/messages")
    fun getConversationWithMessages(
        @LoginUser userId: Long,
        @Parameter(description = "대화 ID", required = true)
        @PathVariable conversationId: Long,
        @Parameter(description = "커서 ID (이전 응답의 nextCursor 값)", required = false)
        @RequestParam(required = false) cursor: Long?,
        @Parameter(description = "조회 개수 (기본값: 6)", required = false)
        @RequestParam(defaultValue = "6") size: Int
    ): ResponseEntity<MessageListResponse>
}
