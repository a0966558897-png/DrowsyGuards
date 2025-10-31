package com.patrick.ui.fatigue

import android.app.Application
import com.patrick.core.FatigueLevel
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations

class FatigueViewModelResetTest {
    @Mock
    private lateinit var mockApplication: Application

    private lateinit var fatigueViewModel: FatigueViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        fatigueViewModel = FatigueViewModel(mockApplication)
    }

    @Test
    fun `test handleUserAcknowledged resets fatigue events`() =
        runTest {
            // Given: 模擬疲勞事件計數
            val initialCount = 5
            // 這裡需要模擬 FatigueDetectionManager 的行為

            // When: 用戶確認已清醒
            fatigueViewModel.handleUserAcknowledged()

            // Then: 驗證狀態重置
            assertEquals(FatigueLevel.NORMAL, fatigueViewModel.fatigueLevel.value)
            assertFalse(fatigueViewModel.showFatigueDialog.value)
            assertEquals("持續偵測中…", fatigueViewModel.statusText.value)
        }

    @Test
    fun `test handleUserRequestedRest resets fatigue events`() =
        runTest {
            // Given: 模擬疲勞事件計數

            // When: 用戶要求休息
            fatigueViewModel.handleUserRequestedRest()

            // Then: 驗證狀態重置
            assertEquals(FatigueLevel.NORMAL, fatigueViewModel.fatigueLevel.value)
            assertFalse(fatigueViewModel.showFatigueDialog.value)
            assertEquals("持續偵測中…", fatigueViewModel.statusText.value)
        }

    @Test
    fun `test reset status info is available`() {
        // When: 獲取重置狀態信息
        val statusInfo = fatigueViewModel.getResetStatusInfo()

        // Then: 驗證狀態信息不為空
        assertFalse(statusInfo.isEmpty())
        assertTrue(statusInfo.contains("ResetProtection"))
        assertTrue(statusInfo.contains("Cooldown"))
    }
} 
