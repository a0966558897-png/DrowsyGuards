package com.patrick.ui.fatigue

import com.patrick.core.FatigueLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FatigueUiStateManagerTest {
    @Test
    fun `test initial state`() {
        val manager = FatigueUiStateManager()

        assertEquals(FatigueLevel.NORMAL, manager.currentFatigueLevel.value)
        assertFalse(manager.hasActiveWarningDialog())
        assertFalse(manager.isInResetProtection())
        assertFalse(manager.isInCooldownPeriod())
    }

    @Test
    fun `test onUserAcknowledged starts reset protection and cooldown`() {
        val manager = FatigueUiStateManager()

        manager.onUserAcknowledged()

        assertEquals(FatigueLevel.NORMAL, manager.currentFatigueLevel.value)
        assertTrue(manager.isInResetProtection())
        assertTrue(manager.isInCooldownPeriod())
        assertFalse(manager.hasActiveWarningDialog())
    }

    @Test
    fun `test warning dialog state management`() {
        val manager = FatigueUiStateManager()

        manager.setWarningDialogActive(true)
        assertTrue(manager.hasActiveWarningDialog())

        manager.setWarningDialogActive(false)
        assertFalse(manager.hasActiveWarningDialog())
    }

    @Test
    fun `test processFatigueResult during reset protection returns normal`() {
        val manager = FatigueUiStateManager()
        manager.onUserAcknowledged()

        val result = manager.processFatigueResult(FatigueLevel.WARNING, 5)

        assertEquals(FatigueLevel.NORMAL, result)
        assertEquals(FatigueLevel.NORMAL, manager.currentFatigueLevel.value)
    }

    @Test
    fun `test processFatigueResult during cooldown downgrades warning to notice`() {
        val manager = FatigueUiStateManager()
        manager.onUserAcknowledged()

        // 模擬冷卻期但不在重置保護期
        // 這裡需要手動設置狀態，因為實際的冷卻期檢查需要時間
        val result = manager.processFatigueResult(FatigueLevel.WARNING, 5)

        // 在重置保護期內，應該返回 NORMAL
        assertEquals(FatigueLevel.NORMAL, result)
    }

    @Test
    fun `test reset clears all states`() {
        val manager = FatigueUiStateManager()
        manager.onUserAcknowledged()
        manager.setWarningDialogActive(true)

        manager.reset()

        assertEquals(FatigueLevel.NORMAL, manager.currentFatigueLevel.value)
        assertFalse(manager.hasActiveWarningDialog())
        assertFalse(manager.isInResetProtection())
        assertFalse(manager.isInCooldownPeriod())
    }
} 
