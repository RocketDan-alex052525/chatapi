package com.rkd.chatapi.conversation.controller

import com.rkd.chatapi.common.annotation.LoginUser
import com.rkd.chatapi.conversation.dto.response.ConversationListResponse
import com.rkd.chatapi.conversation.service.ConversationInfoService
import com.rkd.chatapi.message.dto.response.MessageListResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/conversations")
class ConversationInfoController(
    private val conversationInfoService: ConversationInfoService
) : ConversationInfoApi {
    override fun getConversations(
        @LoginUser userId: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "5") size: Int
    ): ResponseEntity<ConversationListResponse> {
        val conversationsGetResponse = conversationInfoService.getConversations(userId, cursor, size)
        return ResponseEntity.ok(conversationsGetResponse)
    }

    override fun getConversationWithMessages(
        @LoginUser userId: Long,
        @PathVariable conversationId: Long,
        @RequestParam(required = false) cursor: Long?,
        @RequestParam(defaultValue = "6") size: Int
    ): ResponseEntity<MessageListResponse> {
        val messagesGetResponse = conversationInfoService.getConversationWithMessages(userId, conversationId, cursor, size)
        return ResponseEntity.ok(messagesGetResponse)
    }
}
