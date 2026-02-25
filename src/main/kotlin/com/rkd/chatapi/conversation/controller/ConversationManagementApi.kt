package com.rkd.chatapi.conversation.controller

import com.rkd.chatapi.common.annotation.LoginUser
import com.rkd.chatapi.conversation.dto.request.ConversationCreateRequest
import com.rkd.chatapi.conversation.dto.response.ConversationCreateResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Tag(name = "Conversation", description = "대화 관리 API")
interface ConversationManagementApi {

    @Operation(
        summary = "대화 생성",
        description = "새 대화를 생성합니다.",
        security = [SecurityRequirement(name = "cookieAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "생성 성공"),
        ApiResponse(responseCode = "401", description = "인증 실패 — 액세스 토큰 없음 (JWT0002)"),
    )
    @PostMapping
    fun createConversation(
        @RequestBody conversationCreateRequest: ConversationCreateRequest,
        @LoginUser userId: Long
    ): ResponseEntity<ConversationCreateResponse>

    @Operation(
        summary = "대화 삭제",
        description = "특정 대화와 해당 대화의 모든 메시지를 삭제합니다.",
        security = [SecurityRequirement(name = "cookieAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "204", description = "삭제 성공"),
        ApiResponse(responseCode = "400", description = "대화를 찾을 수 없음 (CONVERSATION0001)"),
        ApiResponse(responseCode = "401", description = "인증 실패 — 액세스 토큰 없음 (JWT0002)"),
        ApiResponse(responseCode = "403", description = "대화 접근 권한 없음 (CONVERSATION0002)"),
    )
    @DeleteMapping("/{conversationId}")
    fun deleteConversation(
        @LoginUser userId: Long,
        @Parameter(description = "대화 ID", required = true)
        @PathVariable conversationId: Long
    ): ResponseEntity<Void>
}
