package com.patrick.alert

import android.content.Context
import android.media.MediaPlayer
import com.patrick.core.BaseTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * SoundManager 單元測試
 */
class SoundManagerTest : BaseTest() {
    private lateinit var soundManager: SoundManager
    private lateinit var mockContext: Context
    private lateinit var mockMediaPlayer: MediaPlayer

    @BeforeEach
    fun setUp() {
        super.setUp()
        MockKAnnotations.init(this)

        mockContext = mockk(relaxed = true)
        mockMediaPlayer = mockk(relaxed = true)
        soundManager = SoundManager(mockContext)

        // Mock MediaPlayer.create
        mockkStatic(MediaPlayer::class)
        every { MediaPlayer.create(any(), any()) } returns mockMediaPlayer
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `playWarningSound should create and start MediaPlayer`() =
        runTest {
            // Given
            every { mockContext.resources.getIdentifier("warning", "raw", "com.patrick.drowsyguard") } returns 123

            // When
            soundManager.playWarningSound()

            // Then
            verify {
                MediaPlayer.create(mockContext, 123)
                mockMediaPlayer.isLooping = false
                mockMediaPlayer.setOnCompletionListener(any())
                mockMediaPlayer.start()
            }
        }

    @Test
    fun `playEmergencySound should create and start MediaPlayer with looping`() =
        runTest {
            // Given
            every { mockContext.resources.getIdentifier("emergency", "raw", "com.patrick.drowsyguard") } returns 456

            // When
            soundManager.playEmergencySound()

            // Then
            verify {
                MediaPlayer.create(mockContext, 456)
                mockMediaPlayer.isLooping = true
                mockMediaPlayer.setOnCompletionListener(any())
                mockMediaPlayer.start()
            }
        }

    @Test
    fun `stopAllSounds should release MediaPlayer`() =
        runTest {
            // Given
            every { mockMediaPlayer.isPlaying } returns true

            // When
            soundManager.stopAllSounds()

            // Then
            verify {
                mockMediaPlayer.stop()
                mockMediaPlayer.release()
            }
        }

    @Test
    fun `playWarningSound should handle resource not found`() =
        runTest {
            // Given
            every { mockContext.resources.getIdentifier("warning", "raw", "com.patrick.drowsyguard") } returns 0

            // When & Then
            assertDoesNotThrow {
                soundManager.playWarningSound()
            }

            verify(exactly = 0) {
                MediaPlayer.create(any(), any())
            }
        }

    @Test
    fun `playEmergencySound should handle resource not found`() =
        runTest {
            // Given
            every { mockContext.resources.getIdentifier("emergency", "raw", "com.patrick.drowsyguard") } returns 0

            // When & Then
            assertDoesNotThrow {
                soundManager.playEmergencySound()
            }

            verify(exactly = 0) {
                MediaPlayer.create(any(), any())
            }
        }

    @Test
    fun `playWarningSound should handle MediaPlayer creation failure`() =
        runTest {
            // Given
            every { mockContext.resources.getIdentifier("warning", "raw", "com.patrick.drowsyguard") } returns 123
            every { MediaPlayer.create(any(), any()) } returns null

            // When & Then
            assertDoesNotThrow {
                soundManager.playWarningSound()
            }
        }

    @Test
    fun `playEmergencySound should handle MediaPlayer creation failure`() =
        runTest {
            // Given
            every { mockContext.resources.getIdentifier("emergency", "raw", "com.patrick.drowsyguard") } returns 456
            every { MediaPlayer.create(any(), any()) } returns null

            // When & Then
            assertDoesNotThrow {
                soundManager.playEmergencySound()
            }
        }
}
