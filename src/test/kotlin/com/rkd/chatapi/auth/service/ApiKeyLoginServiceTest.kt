package com.rkd.chatapi.auth.service

import com.rkd.chatapi.auth.exception.ApiKeyInvalidException
import com.rkd.chatapi.auth.validator.ApiKeyValidator
import com.rkd.chatapi.common.security.ApiKeyHasher
import com.rkd.chatapi.common.security.JwtTokenProvider
import com.rkd.chatapi.user.service.UserInfoService
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class ApiKeyLoginServiceTest {

    @InjectMocks
    private lateinit var apiKeyLoginService: ApiKeyLoginService

    @Mock
    private lateinit var apiKeyHasher: ApiKeyHasher

    @Mock
    private lateinit var apiKeyValidator: ApiKeyValidator

    @Mock
    private lateinit var userInfoService: UserInfoService

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    fun `login returns LoginResponse on success`() {
        // given
        val rawKey = "sk-test-key"
        whenever(apiKeyHasher.hash(rawKey)).thenReturn("hashed-key")
        whenever(userInfoService.findUserByApiKey("hashed-key")).thenReturn(1L)
        whenever(jwtTokenProvider.createToken(1L)).thenReturn("jwt-token")
        whenever(jwtTokenProvider.getExpirationSeconds()).thenReturn(900L)

        // when
        val result = apiKeyLoginService.login(rawKey)

        // then
        assertThat(result.userId).isEqualTo(1L)
        assertThat(result.accessToken).isEqualTo("jwt-token")
        assertThat(result.expiresInSeconds).isEqualTo(900L)
    }

    @Test
    fun `login throws ApiKeyInvalidException when validation fails`() {
        // given
        val rawKey = "invalid-key"
        doThrow(ApiKeyInvalidException()).whenever(apiKeyValidator).validateApiKey(rawKey)

        // when & then
        assertThrows<ApiKeyInvalidException> { apiKeyLoginService.login(rawKey) }
    }
}
