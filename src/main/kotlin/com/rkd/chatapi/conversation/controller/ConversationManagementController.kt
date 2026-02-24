package com.rkd.chatapi.conversation.controller

import com.rkd.chatapi.common.annotation.LoginUser
import com.rkd.chatapi.conversation.dto.request.ConversationCreateRequest
import com.rkd.chatapi.conversation.dto.response.ConversationCreateResponse
import com.rkd.chatapi.conversation.service.ConversationManagementService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/conversations")
class ConversationManagementController(
    private val conversationManagementService: ConversationManagementService
) : ConversationManagementApi {
    override fun createConversation(
        @RequestBody conversationCreateRequest: ConversationCreateRequest,
        @LoginUser userId: Long
    ): ResponseEntity<ConversationCreateResponse> {
        val conversationCreateResponse = conversationManagementService.createConversation(userId, conversationCreateRequest)
        return ResponseEntity.ok(conversationCreateResponse)
    }

    override fun deleteConversation(
        @LoginUser userId: Long,
        @PathVariable conversationId: Long
    ): ResponseEntity<Void> {
        conversationManagementService.deleteConversation(userId, conversationId)
        return ResponseEntity.noContent().build()
    }
}
