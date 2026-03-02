package com.rkd.chatapi.auth.service

import com.rkd.chatapi.auth.exception.ApiKeyInvalidException
import com.rkd.chatapi.auth.validator.ApiKeyValidator
import com.rkd.chatapi.common.security.ApiKeyEncryptor
import com.rkd.chatapi.common.security.ApiKeyHasher
import com.rkd.chatapi.user.service.UserManagementService
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
class ApiKeyManagementServiceTest {

    @InjectMocks
    private lateinit var apiKeyManagementService: ApiKeyManagementService

    @Mock
    private lateinit var apiKeyHasher: ApiKeyHasher

    @Mock
    private lateinit var apiKeyValidator: ApiKeyValidator

    @Mock
    private lateinit var apiKeyEncryptor: ApiKeyEncryptor

    @Mock
    private lateinit var userManagementService: UserManagementService

    @Test
    fun `registerApiKey returns userId on success`() {
        // given
        val rawKey = "sk-test-key"
        whenever(apiKeyHasher.hash(rawKey)).thenReturn("hashed-key")
        whenever(apiKeyEncryptor.encrypt(rawKey)).thenReturn("enc-key")
        whenever(userManagementService.createUserByApiKey("hashed-key", "enc-key")).thenReturn(1L)

        // when
        val result = apiKeyManagementService.registerApiKey(rawKey)

        // then
        assertThat(result.userId).isEqualTo(1L)
    }

    @Test
    fun `registerApiKey throws ApiKeyInvalidException when validation fails`() {
        // given
        val rawKey = "invalid-key"
        doThrow(ApiKeyInvalidException()).whenever(apiKeyValidator).validateApiKey(rawKey)

        // when & then
        assertThrows<ApiKeyInvalidException> { apiKeyManagementService.registerApiKey(rawKey) }
    }
}
