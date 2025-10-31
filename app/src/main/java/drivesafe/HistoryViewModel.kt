package com.example.drivesafe

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.patrick.main.ui.FatigueUiItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * 把 Repository 的 Flow 暴露為 StateFlow 給 Compose 使用。
 * 放在 com.example.drivesafe（無子資料夾）。
 */
class HistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = HistoryRepository.get(app)

    private val _uiItems = MutableStateFlow<List<FatigueUiItem>>(emptyList())
    val uiItems: StateFlow<List<FatigueUiItem>> = _uiItems

    init {
        viewModelScope.launch {
            repo.recentFlow(limit = 50)
                .onStart { /* TODO: 需要可顯示 loading 狀態 */ }
                .catch { /* TODO: 記錄錯誤或上報 */ }
                .collect { _uiItems.value = it }
        }
    }
}
