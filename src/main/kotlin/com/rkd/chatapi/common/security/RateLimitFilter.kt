package com.rkd.chatapi.common.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.rkd.chatapi.common.error.ErrorCode
import com.rkd.chatapi.common.error.ErrorResponse
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.concurrent.TimeUnit

@Component
class RateLimitFilter(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${rate-limit.chat.limit}") private val limit: Long,
    @Value("\${rate-limit.chat.window-seconds}") private val windowSeconds: Long
) : OncePerRequestFilter() {

    companion object {
        private val RATE_LIMITED_PATHS = setOf(
            "/api/chat/completions",
            "/api/chat/completions/stream"
        )
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        request.servletPath !in RATE_LIMITED_PATHS

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val userId = SecurityContextHolder.getContext().authentication?.principal as? Long
            ?: run {
                filterChain.doFilter(request, response)
                return
            }

        val key = "rate:chat:$userId"
        val count = redisTemplate.opsForValue().increment(key) ?: 1L
        if (count == 1L) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS)
        }

        if (count > limit) {
            val errorCode = ErrorCode.RATE_LIMIT_EXCEEDED
            response.status = errorCode.httpStatus.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"
            objectMapper.writeValue(response.writer, ErrorResponse.of(errorCode))
            return
        }

        filterChain.doFilter(request, response)
    }
}
