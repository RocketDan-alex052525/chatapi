package com.rkd.chatapi.common.config

import com.rkd.chatapi.common.security.ApiKeyAuthFilter
import com.rkd.chatapi.common.security.JwtAuthFilter
import com.rkd.chatapi.common.security.RateLimitFilter
import jakarta.servlet.DispatcherType
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.security.web.util.matcher.DispatcherTypeRequestMatcher

@Configuration
class SecurityConfig(
    private val apiKeyAuthFilter: ApiKeyAuthFilter,
    private val jwtAuthFilter: JwtAuthFilter,
    private val rateLimitFilter: RateLimitFilter
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http {
            csrf { disable() }
            formLogin { disable() }
            httpBasic { disable() }
            sessionManagement {
                sessionCreationPolicy = SessionCreationPolicy.STATELESS
            }
            authorizeHttpRequests {
                authorize(DispatcherTypeRequestMatcher(DispatcherType.ASYNC), permitAll)
                authorize("/api/auth/**", permitAll)
                authorize("/management/health", permitAll)
                authorize("/api/**", authenticated)
                authorize(anyRequest, permitAll)
            }
            addFilterBefore<UsernamePasswordAuthenticationFilter>(apiKeyAuthFilter)
            addFilterBefore<UsernamePasswordAuthenticationFilter>(jwtAuthFilter)
            addFilterAfter<JwtAuthFilter>(rateLimitFilter)
        }
        return http.build()
    }
}
