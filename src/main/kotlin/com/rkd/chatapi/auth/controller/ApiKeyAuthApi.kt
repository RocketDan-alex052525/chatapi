package com.rkd.chatapi.auth.controller

import com.rkd.chatapi.auth.dto.response.ApiKeyRegisterResponse
import com.rkd.chatapi.auth.dto.response.LoginResponse
import com.rkd.chatapi.common.security.ApiKeyAuthFilter.Companion.API_KEY_HEADER
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader

@Tag(name = "Auth", description = "API Key 등록 및 로그인")
interface ApiKeyAuthApi {

    @Operation(
        summary = "API Key 등록",
        description = "OpenAI API Key를 등록합니다. 등록 전 OpenAI에 유효성 검증을 수행합니다.",
        security = [SecurityRequirement(name = "apiKeyAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "등록 성공"),
        ApiResponse(responseCode = "400", description = "유효하지 않은 API Key (AUTH0001) 또는 이미 등록된 유저 (USER0001)"),
    )
    @PostMapping("/apiKey")
    fun registerApiKey(
        @Parameter(description = "OpenAI API Key", required = true)
        @RequestHeader(API_KEY_HEADER) apiKey: String
    ): ResponseEntity<ApiKeyRegisterResponse>

    @Operation(
        summary = "로그인",
        description = "API Key로 인증 후 JWT 액세스 토큰을 쿠키로 발급합니다.",
        security = [SecurityRequirement(name = "apiKeyAuth")]
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "로그인 성공 (ACCESS_TOKEN_COOKIE 쿠키 발급)"),
        ApiResponse(responseCode = "400", description = "API Key가 존재하지 않음 (AUTH0002) 또는 유효하지 않은 토큰 (JWT0001)"),
    )
    @GetMapping("/login")
    fun login(
        @Parameter(description = "OpenAI API Key", required = true)
        @RequestHeader(API_KEY_HEADER) apiKey: String
    ): ResponseEntity<LoginResponse>
}
