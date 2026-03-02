package com.rkd.chatapi.user.domain

import com.rkd.chatapi.user.domain.entity.User
import com.rkd.chatapi.user.domain.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class UserWriterTest {

    @InjectMocks
    private lateinit var userWriter: UserWriter

    @Mock
    private lateinit var userRepository: UserRepository

    private val user = User(apiKey = "hashed-key", apiKeyEnc = "enc-key")

    @Test
    fun `save returns saved user`() {
        // given
        whenever(userRepository.save(user)).thenReturn(user)

        // when
        val result = userWriter.save(user)

        // then
        assertThat(result).isEqualTo(user)
    }
}
