package com.patrick.main.ui

import androidx.camera.view.PreviewView
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.patrick.core.FatigueLevel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * FatigueMainScreen UI 測試
 */
@RunWith(AndroidJUnit4::class)
class FatigueMainScreenUITest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `should display normal status when fatigue level is normal`() {
        // Given
        val previewView = PreviewView(composeTestRule.activity)

        // When
        composeTestRule.setContent {
            FatigueMainScreen(
                fatigueLevel = FatigueLevel.NORMAL,
                calibrationProgress = 0,
                isCalibrating = false,
                showFatigueDialog = false,
                previewView = previewView,
            )
        }

        // Then
        composeTestRule.onNodeWithText("持續偵測中…").assertIsDisplayed()
    }

    @Test
    fun `should display notice status when fatigue level is notice`() {
        // Given
        val previewView = PreviewView(composeTestRule.activity)

        // When
        composeTestRule.setContent {
            FatigueMainScreen(
                fatigueLevel = FatigueLevel.NOTICE,
                calibrationProgress = 0,
                isCalibrating = false,
                showFatigueDialog = false,
                previewView = previewView,
            )
        }

        // Then
        composeTestRule.onNodeWithText("提醒：偵測到疲勞行為").assertIsDisplayed()
    }

    @Test
    fun `should display warning status when fatigue level is warning`() {
        // Given
        val previewView = PreviewView(composeTestRule.activity)

        // When
        composeTestRule.setContent {
            FatigueMainScreen(
                fatigueLevel = FatigueLevel.WARNING,
                calibrationProgress = 0,
                isCalibrating = false,
                showFatigueDialog = false,
                previewView = previewView,
            )
        }

        // Then
        composeTestRule.onNodeWithText("警告：請確認您的狀態").assertIsDisplayed()
    }

    @Test
    fun `should display calibration progress when calibrating`() {
        // Given
        val previewView = PreviewView(composeTestRule.activity)

        // When
        composeTestRule.setContent {
            FatigueMainScreen(
                fatigueLevel = FatigueLevel.NORMAL,
                calibrationProgress = 50,
                isCalibrating = true,
                showFatigueDialog = false,
                previewView = previewView,
            )
        }

        // Then
        composeTestRule.onNodeWithText("正在校正中...").assertIsDisplayed()
        composeTestRule.onNodeWithText("校正中… 50%").assertIsDisplayed()
    }

    @Test
    fun `should show fatigue dialog when showFatigueDialog is true`() {
        // Given
        val previewView = PreviewView(composeTestRule.activity)

        // When
        composeTestRule.setContent {
            FatigueMainScreen(
                fatigueLevel = FatigueLevel.WARNING,
                calibrationProgress = 0,
                isCalibrating = false,
                showFatigueDialog = true,
                previewView = previewView,
            )
        }

        // Then
        composeTestRule.onNodeWithText("疲勞警告").assertIsDisplayed()
        composeTestRule.onNodeWithText("我已清醒").assertIsDisplayed()
        composeTestRule.onNodeWithText("我會找地方休息").assertIsDisplayed()
    }

    @Test
    fun `should not show fatigue dialog when showFatigueDialog is false`() {
        // Given
        val previewView = PreviewView(composeTestRule.activity)

        // When
        composeTestRule.setContent {
            FatigueMainScreen(
                fatigueLevel = FatigueLevel.WARNING,
                calibrationProgress = 0,
                isCalibrating = false,
                showFatigueDialog = false,
                previewView = previewView,
            )
        }

        // Then
        composeTestRule.onNodeWithText("疲勞警告").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("我已清醒").assertIsNotDisplayed()
        composeTestRule.onNodeWithText("我會找地方休息").assertIsNotDisplayed()
    }

    @Test
    fun `should display menu button`() {
        // Given
        val previewView = PreviewView(composeTestRule.activity)

        // When
        composeTestRule.setContent {
            FatigueMainScreen(
                fatigueLevel = FatigueLevel.NORMAL,
                calibrationProgress = 0,
                isCalibrating = false,
                showFatigueDialog = false,
                previewView = previewView,
            )
        }

        // Then
        composeTestRule.onNodeWithText("☰").assertIsDisplayed()
    }
}
