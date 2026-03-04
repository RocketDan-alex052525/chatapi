package com.rkd.chatapi.conversation.lock

import com.rkd.chatapi.conversation.exception.ConversationBusyException
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.concurrent.TimeUnit

@Component
class ConversationLockManager(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val LOCK_PREFIX = "lock:conversation:"
        private const val LOCK_TTL_SECONDS = 30L
    }

    // 동기 처리용 (일반 채팅)
    fun <T> withLock(conversationId: Long, action: () -> T): T {
        val (key, value) = acquire(conversationId)
        try {
            return action()
        } finally {
            release(key, value)
        }
    }

    // 비동기 처리용 (스트리밍) — 락 획득 후 토큰 반환
    fun tryAcquire(conversationId: Long): Pair<String, String> {
        return acquire(conversationId)
    }

    fun releaseLock(lockToken: Pair<String, String>) {
        release(lockToken.first, lockToken.second)
    }

    private fun acquire(conversationId: Long): Pair<String, String> {
        val key = "$LOCK_PREFIX$conversationId"
        val value = UUID.randomUUID().toString()

        val acquired = redisTemplate.opsForValue()
            .setIfAbsent(key, value, LOCK_TTL_SECONDS, TimeUnit.SECONDS) ?: false

        if (!acquired) throw ConversationBusyException()

        return Pair(key, value)
    }

    private fun release(key: String, value: String) {
        if (redisTemplate.opsForValue().get(key) == value) {
            redisTemplate.delete(key)
        }
    }
}
