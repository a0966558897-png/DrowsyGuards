package com.patrick.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

/**
 * 測試基礎類
 * 提供通用的測試設置和工具方法
 */
@OptIn(ExperimentalCoroutinesApi::class)
abstract class BaseTest {
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * 獲取測試調度器
     */
    protected fun getTestDispatcher(): TestDispatcher = testDispatcher
}
