package com.patrick.alert

import android.content.Context
import android.content.pm.PackageManager
import android.os.Vibrator
import com.patrick.core.BaseTest
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * VibrationManager 單元測試
 */
class VibrationManagerTest : BaseTest() {
    private lateinit var vibrationManager: VibrationManager
    private lateinit var mockContext: Context
    private lateinit var mockVibrator: Vibrator

    @BeforeEach
    fun setUp() {
        super.setUp()
        MockKAnnotations.init(this)

        mockContext = mockk(relaxed = true)
        mockVibrator = mockk(relaxed = true)
        vibrationManager = VibrationManager(mockContext)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `triggerVibration should vibrate when permission granted`() =
        runTest {
            // Given
            every { mockContext.checkSelfPermission(android.Manifest.permission.VIBRATE) } returns PackageManager.PERMISSION_GRANTED
            every { mockContext.getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator

            // When
            vibrationManager.triggerVibration()

            // Then
            verify {
                mockVibrator.vibrate(VibrationManager.VIBRATION_DURATION_MS)
            }
        }

    @Test
    fun `triggerVibration should not vibrate when permission denied`() =
        runTest {
            // Given
            every { mockContext.checkSelfPermission(android.Manifest.permission.VIBRATE) } returns PackageManager.PERMISSION_DENIED

            // When
            vibrationManager.triggerVibration()

            // Then
            verify(exactly = 0) {
                mockContext.getSystemService(any())
            }
        }

    @Test
    fun `triggerStrongVibration should vibrate with pattern when permission granted`() =
        runTest {
            // Given
            every { mockContext.checkSelfPermission(android.Manifest.permission.VIBRATE) } returns PackageManager.PERMISSION_GRANTED
            every { mockContext.getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator

            // When
            vibrationManager.triggerStrongVibration()

            // Then
            verify {
                mockVibrator.vibrate(longArrayOf(0, 500, 200, 500), -1)
            }
        }

    @Test
    fun `triggerStrongVibration should not vibrate when permission denied`() =
        runTest {
            // Given
            every { mockContext.checkSelfPermission(android.Manifest.permission.VIBRATE) } returns PackageManager.PERMISSION_DENIED

            // When
            vibrationManager.triggerStrongVibration()

            // Then
            verify(exactly = 0) {
                mockContext.getSystemService(any())
            }
        }

    @Test
    fun `triggerVibration should handle security exception`() =
        runTest {
            // Given
            every { mockContext.checkSelfPermission(android.Manifest.permission.VIBRATE) } returns PackageManager.PERMISSION_GRANTED
            every { mockContext.getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator
            every { mockVibrator.vibrate(any()) } throws SecurityException("Permission denied")

            // When & Then
            assertDoesNotThrow {
                vibrationManager.triggerVibration()
            }
        }

    @Test
    fun `triggerStrongVibration should handle security exception`() =
        runTest {
            // Given
            every { mockContext.checkSelfPermission(android.Manifest.permission.VIBRATE) } returns PackageManager.PERMISSION_GRANTED
            every { mockContext.getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator
            every { mockVibrator.vibrate(any(), any()) } throws SecurityException("Permission denied")

            // When & Then
            assertDoesNotThrow {
                vibrationManager.triggerStrongVibration()
            }
        }

    @Test
    fun `triggerVibration should handle illegal argument exception`() =
        runTest {
            // Given
            every { mockContext.checkSelfPermission(android.Manifest.permission.VIBRATE) } returns PackageManager.PERMISSION_GRANTED
            every { mockContext.getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator
            every { mockVibrator.vibrate(any()) } throws IllegalArgumentException("Invalid duration")

            // When & Then
            assertDoesNotThrow {
                vibrationManager.triggerVibration()
            }
        }

    @Test
    fun `triggerStrongVibration should handle illegal argument exception`() =
        runTest {
            // Given
            every { mockContext.checkSelfPermission(android.Manifest.permission.VIBRATE) } returns PackageManager.PERMISSION_GRANTED
            every { mockContext.getSystemService(Context.VIBRATOR_SERVICE) } returns mockVibrator
            every { mockVibrator.vibrate(any(), any()) } throws IllegalArgumentException("Invalid pattern")

            // When & Then
            assertDoesNotThrow {
                vibrationManager.triggerStrongVibration()
            }
        }
}
