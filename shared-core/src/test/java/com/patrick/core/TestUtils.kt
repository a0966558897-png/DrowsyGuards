package com.patrick.core

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 * 測試工具類
 * 提供常用的測試工具方法
 */
object TestUtils {
    /**
     * 獲取測試 Context
     */
    fun getTestContext(): Context = InstrumentationRegistry.getInstrumentation().targetContext

    /**
     * 創建測試用的疲勞檢測結果
     */
    fun createFatigueDetectionResult(
        isFatigueDetected: Boolean = false,
        fatigueLevel: FatigueLevel = FatigueLevel.NORMAL,
        confidence: Float = 0.5f,
    ): FatigueDetectionResult {
        return FatigueDetectionResult(
            isFatigueDetected = isFatigueDetected,
            fatigueLevel = fatigueLevel,
            confidence = confidence,
            earValue = 0.3f,
            timestamp = System.currentTimeMillis(),
        )
    }

    /**
     * 創建正常狀態的疲勞檢測結果
     */
    fun createNormalFatigueResult(): FatigueDetectionResult {
        return createFatigueDetectionResult(
            isFatigueDetected = false,
            fatigueLevel = FatigueLevel.NORMAL,
            confidence = 0.8f,
        )
    }

    /**
     * 創建注意狀態的疲勞檢測結果
     */
    fun createNoticeFatigueResult(): FatigueDetectionResult {
        return createFatigueDetectionResult(
            isFatigueDetected = true,
            fatigueLevel = FatigueLevel.NOTICE,
            confidence = 0.6f,
        )
    }

    /**
     * 創建警告狀態的疲勞檢測結果
     */
    fun createWarningFatigueResult(): FatigueDetectionResult {
        return createFatigueDetectionResult(
            isFatigueDetected = true,
            fatigueLevel = FatigueLevel.WARNING,
            confidence = 0.9f,
        )
    }
}

/**
 * MockK 測試基礎類
 */
abstract class MockKTest {
    @BeforeEach
    fun setUpMockK() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDownMockK() {
        unmockkAll()
    }
}
