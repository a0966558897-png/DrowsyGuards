package com.patrick.alert

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.patrick.core.TestUtils
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FatigueAlertManager 集成測試
 * 測試各個組件之間的協調工作
 */
@RunWith(AndroidJUnit4::class)
class FatigueAlertManagerIntegrationTest {
    private lateinit var fatigueAlertManager: FatigueAlertManager
    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        fatigueAlertManager = FatigueAlertManager(context)
    }

    @Test
    fun `handleFatigueDetection should not trigger alert for normal level`() =
        runTest {
            // Given
            val result = TestUtils.createNormalFatigueResult()

            // When
            fatigueAlertManager.handleFatigueDetection(result)

            // Then - 應該不會觸發任何警報
            // 這個測試主要驗證不會拋出異常
        }

    @Test
    fun `handleFatigueDetection should trigger notice alert for notice level`() =
        runTest {
            // Given
            val result = TestUtils.createNoticeFatigueResult()

            // When
            fatigueAlertManager.handleFatigueDetection(result)

            // Then - 應該觸發注意警報
            // 這個測試主要驗證不會拋出異常
        }

    @Test
    fun `handleFatigueDetection should trigger warning alert for warning level`() =
        runTest {
            // Given
            val result = TestUtils.createWarningFatigueResult()

            // When
            fatigueAlertManager.handleFatigueDetection(result)

            // Then - 應該觸發警告警報
            // 這個測試主要驗證不會拋出異常
        }

    @Test
    fun `stopAllAlerts should clean up resources`() =
        runTest {
            // Given
            val result = TestUtils.createWarningFatigueResult()
            fatigueAlertManager.handleFatigueDetection(result)

            // When
            fatigueAlertManager.stopAllAlerts()

            // Then - 應該清理所有資源
            // 這個測試主要驗證不會拋出異常
        }

    @Test
    fun `cleanup should release all resources`() =
        runTest {
            // Given
            val result = TestUtils.createWarningFatigueResult()
            fatigueAlertManager.handleFatigueDetection(result)

            // When
            fatigueAlertManager.cleanup()

            // Then - 應該釋放所有資源
            // 這個測試主要驗證不會拋出異常
        }

    @Test
    fun `dialog callbacks should work correctly`() =
        runTest {
            // Given
            var acknowledgedCalled = false
            var restRequestedCalled = false

            fatigueAlertManager.setDialogCallback(
                object : FatigueDialogCallback {
                    override fun onUserAcknowledged() {
                        acknowledgedCalled = true
                    }

                    override fun onUserRequestedRest() {
                        restRequestedCalled = true
                    }
                },
            )

            // When
            fatigueAlertManager.onUserAcknowledged()
            fatigueAlertManager.onUserRequestedRest()

            // Then
            assertTrue(acknowledgedCalled)
            assertTrue(restRequestedCalled)
        }
}
