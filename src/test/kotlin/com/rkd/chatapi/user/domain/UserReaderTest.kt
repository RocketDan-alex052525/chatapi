package com.rkd.chatapi.user.domain

import com.rkd.chatapi.user.domain.entity.User
import com.rkd.chatapi.user.domain.repository.UserRepository
import com.rkd.chatapi.user.exception.UserNotExistException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class UserReaderTest {

    @InjectMocks
    private lateinit var userReader: UserReader

    @Mock
    private lateinit var userRepository: UserRepository

    private val user = User(apiKey = "hashed-key", apiKeyEnc = "enc-key")

    @Test
    fun `findUserById returns user when found`() {
        // given
        whenever(userRepository.findById(1L)).thenReturn(Optional.of(user))

        // when
        val result = userReader.findUserById(1L)

        // then
        assertThat(result).isEqualTo(user)
    }

    @Test
    fun `findUserById throws UserNotExistException when not found`() {
        // given
        whenever(userRepository.findById(1L)).thenReturn(Optional.empty())

        // when & then
        assertThrows<UserNotExistException> { userReader.findUserById(1L) }
    }

    @Test
    fun `findUserByApiKey returns user when found`() {
        // given
        whenever(userRepository.findByApiKey("hashed-key")).thenReturn(user)

        // when
        val result = userReader.findUserByApiKey("hashed-key")

        // then
        assertThat(result).isEqualTo(user)
    }

    @Test
    fun `findUserByApiKey returns null when not found`() {
        // given
        whenever(userRepository.findByApiKey("missing-key")).thenReturn(null)

        // when
        val result = userReader.findUserByApiKey("missing-key")

        // then
        assertThat(result).isNull()
    }
}
