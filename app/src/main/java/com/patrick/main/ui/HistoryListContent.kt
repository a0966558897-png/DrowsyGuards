package com.patrick.main.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class FatigueUiItem(
    val id: Long,
    val timestampMillis: Long,
    val score: Int,
    val levelLabel: String,
    val detail: String? = null
)

private fun millisToLocalDate(millis: Long): LocalDate =
    Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()

@Composable
fun HistoryListContent(
    items: List<FatigueUiItem>,
    modifier: Modifier = Modifier,
    showDividers: Boolean = true,
    onItemClick: (FatigueUiItem) -> Unit = {}
) {
    val grouped = items.groupBy { millisToLocalDate(it.timestampMillis) }
        .toSortedMap(compareByDescending { it })

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        grouped.forEach { (date, dayItems) ->
            // 用一般 item 當日期標頭（非 sticky）
            item(key = "header-$date") {
                HistoryDateHeader(date.toString())
            }
            itemsIndexed(dayItems, key = { _, it -> it.id }) { idx, item ->
                HistoryRow(item = item, onClick = { onItemClick(item) })
                if (showDividers && idx != dayItems.lastIndex) {
                    Divider(modifier = Modifier.padding(start = 12.dp))
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun HistoryDateHeader(dateText: String) {
    Surface(color = MaterialTheme.colorScheme.surface) {
        Text(
            text = dateText, // ex: 2025-10-29
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp, bottom = 6.dp),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun HistoryRow(
    item: FatigueUiItem,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        Row(Modifier.fillMaxWidth()) {
            Text(
                text = "分數 ${item.score} · ${item.levelLabel}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = epochToClock(item.timestampMillis),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (!item.detail.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = item.detail!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun epochToClock(millis: Long): String {
    val t = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime()
    return "%02d:%02d".format(t.hour, t.minute)
}
