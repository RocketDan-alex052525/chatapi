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
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

@Component
class RateLimitFilter(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,
    @Value("\${rate-limit.chat.minute.limit}") private val minuteLimit: Long,
    @Value("\${rate-limit.chat.hour.limit}") private val hourLimit: Long,
    @Value("\${rate-limit.chat.daily.limit}") private val dailyLimit: Long,
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

        val minuteCount = incrementAndSetExpiry("rate:chat:$userId:1m", 60L)
        val hourCount   = incrementAndSetExpiry("rate:chat:$userId:1h", 3600L)
        val dailyCount  = incrementAndSetExpiry("rate:chat:$userId:daily", secondsUntilMidnight())

        val exceededErrorCode = when {
            minuteCount > minuteLimit -> ErrorCode.RATE_LIMIT_EXCEEDED_MINUTE
            hourCount   > hourLimit   -> ErrorCode.RATE_LIMIT_EXCEEDED_HOUR
            dailyCount  > dailyLimit  -> ErrorCode.RATE_LIMIT_EXCEEDED_DAILY
            else                      -> null
        }

        if (exceededErrorCode != null) {
            response.status = exceededErrorCode.httpStatus.value()
            response.contentType = MediaType.APPLICATION_JSON_VALUE
            response.characterEncoding = "UTF-8"
            objectMapper.writeValue(response.writer, ErrorResponse.of(exceededErrorCode))
            return
        }

        filterChain.doFilter(request, response)
    }

    private fun incrementAndSetExpiry(key: String, expireSeconds: Long): Long {
        val count = redisTemplate.opsForValue().increment(key) ?: 1L
        if (count == 1L) {
            redisTemplate.expire(key, expireSeconds, TimeUnit.SECONDS)
        }
        return count
    }

    private fun secondsUntilMidnight(): Long {
        val midnight = LocalDate.now().plusDays(1).atStartOfDay()
        return ChronoUnit.SECONDS.between(LocalDateTime.now(), midnight)
    }
}
