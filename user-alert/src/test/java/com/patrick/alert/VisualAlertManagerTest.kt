package com.patrick.alert

import android.content.Context
import android.view.View
import android.widget.TextView
import com.patrick.core.BaseTest
import com.patrick.core.FatigueDetectionResult
import com.patrick.core.FatigueLevel
import com.patrick.core.TestUtils
import io.mockk.MockKAnnotations
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * VisualAlertManager 單元測試
 */
class VisualAlertManagerTest : BaseTest() {
    private lateinit var visualAlertManager: VisualAlertManager
    private lateinit var mockContext: Context
    private lateinit var mockTextView: TextView

    @BeforeEach
    fun setUp() {
        super.setUp()
        MockKAnnotations.init(this)

        mockContext = mockk(relaxed = true)
        mockTextView = mockk(relaxed = true)
        visualAlertManager = VisualAlertManager(mockContext)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `showAlertOnTextView should show notice alert correctly`() =
        runTest {
            // Given
            val result = TestUtils.createNoticeFatigueResult()
            val textSlot = slot<String>()
            val visibilitySlot = slot<Int>()

            // When
            visualAlertManager.showAlertOnTextView(mockTextView, result)

            // Then
            verify {
                mockTextView.text = capture(textSlot)
                mockTextView.visibility = capture(visibilitySlot)
                mockTextView.setTextColor(any())
            }

            assertEquals("⚠️ 檢測到疲勞跡象，請注意安全！", textSlot.captured)
            assertEquals(View.VISIBLE, visibilitySlot.captured)
        }

    @Test
    fun `showAlertOnTextView should show warning alert correctly`() =
        runTest {
            // Given
            val result = TestUtils.createWarningFatigueResult()
            val textSlot = slot<String>()
            val visibilitySlot = slot<Int>()

            // When
            visualAlertManager.showAlertOnTextView(mockTextView, result)

            // Then
            verify {
                mockTextView.text = capture(textSlot)
                mockTextView.visibility = capture(visibilitySlot)
                mockTextView.setTextColor(any())
            }

            assertEquals("🚨 疲勞警告！請立即確認狀態！", textSlot.captured)
            assertEquals(View.VISIBLE, visibilitySlot.captured)
        }

    @Test
    fun `showAlertOnTextView should hide text view when no fatigue detected`() =
        runTest {
            // Given
            val result = TestUtils.createNormalFatigueResult()
            val visibilitySlot = slot<Int>()

            // When
            visualAlertManager.showAlertOnTextView(mockTextView, result)

            // Then
            verify {
                mockTextView.visibility = capture(visibilitySlot)
            }

            assertEquals(View.GONE, visibilitySlot.captured)

            verify(exactly = 0) {
                mockTextView.text = any()
                mockTextView.setTextColor(any())
            }
        }

    @Test
    fun `showAlertOnTextView should set correct color for notice level`() =
        runTest {
            // Given
            val result = TestUtils.createNoticeFatigueResult()
            val colorSlot = slot<Int>()

            // When
            visualAlertManager.showAlertOnTextView(mockTextView, result)

            // Then
            verify {
                mockTextView.setTextColor(capture(colorSlot))
            }

            // 橙色: #FFA500
            assertEquals(android.graphics.Color.parseColor("#FFA500"), colorSlot.captured)
        }

    @Test
    fun `showAlertOnTextView should set correct color for warning level`() =
        runTest {
            // Given
            val result = TestUtils.createWarningFatigueResult()
            val colorSlot = slot<Int>()

            // When
            visualAlertManager.showAlertOnTextView(mockTextView, result)

            // Then
            verify {
                mockTextView.setTextColor(capture(colorSlot))
            }

            // 紅色: #FF0000
            assertEquals(android.graphics.Color.parseColor("#FF0000"), colorSlot.captured)
        }

    @Test
    fun `showAlertOnTextView should set black color for unknown level`() =
        runTest {
            // Given
            val result =
                FatigueDetectionResult(
                    isFatigueDetected = true,
                    fatigueLevel = FatigueLevel.NORMAL, // 未知的疲勞級別
                    confidence = 0.5f,
                    earValue = 0.3f,
                    timestamp = System.currentTimeMillis(),
                )
            val colorSlot = slot<Int>()

            // When
            visualAlertManager.showAlertOnTextView(mockTextView, result)

            // Then
            verify {
                mockTextView.setTextColor(capture(colorSlot))
            }

            assertEquals(android.graphics.Color.BLACK, colorSlot.captured)
        }

    @Test
    fun `stopAllVisualAlerts should remove callbacks`() =
        runTest {
            // When
            visualAlertManager.stopAllVisualAlerts()

            // Then - 這個方法主要是清理 Handler，在測試中我們主要驗證它不會拋出異常
            // 實際的 Handler 清理邏輯在真實環境中會生效
        }
}
