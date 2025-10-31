package com.patrick.core

import android.content.Context
import android.graphics.Typeface
import android.util.Log

/**
 * 字體載入工具類
 * 負責從 assets 目錄載入字體檔案
 */
object FontLoader {
    private const val TAG = "FontLoader"

    // 字體快取
    private val fontCache = mutableMapOf<String, Typeface>()

    /**
     * 載入字體檔案
     * @param context 上下文
     * @param fontPath 字體檔案路徑（相對於 assets 目錄）
     * @return Typeface 物件，如果載入失敗則返回 null
     */
    fun loadFont(
        context: Context,
        fontPath: String,
    ): Typeface? {
        return try {
            // 檢查快取
            fontCache[fontPath]?.let { return it }
            // 從 assets 載入字體
            val typeface = Typeface.createFromAsset(context.assets, fontPath)
            // 快取字體
            fontCache[fontPath] = typeface
            Log.d(TAG, "成功載入字體: $fontPath")
            typeface
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "字體路徑無效: $fontPath", e)
            null
        } catch (e: RuntimeException) {
            Log.e(TAG, "字體載入運行時錯誤: $fontPath", e)
            null
        } catch (e: Exception) {
            Log.e(TAG, "載入字體時發生未知錯誤: $fontPath", e)
            null
        }
    }

    fun loadNotoSansCJK(context: Context): Typeface? = loadFont(context, Constants.Fonts.NOTO_SANS_CJK)

    fun loadNotoSansCJKBold(context: Context): Typeface? = loadFont(context, Constants.Fonts.NOTO_SANS_CJK_BOLD)

    fun loadNotoSansCJKTC(context: Context): Typeface? = loadFont(context, Constants.Fonts.NOTO_SANS_CJK_TC)

    fun clearCache() {
        fontCache.clear()
        Log.d(TAG, "字體快取已清除")
    }

    fun preloadFonts(context: Context) {
        try {
            loadNotoSansCJK(context)
            loadNotoSansCJKBold(context)
            loadNotoSansCJKTC(context)
            Log.d(TAG, "所有字體預載入完成")
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "預載入字體時字體路徑無效", e)
        } catch (e: RuntimeException) {
            Log.e(TAG, "預載入字體時運行時錯誤", e)
        } catch (e: Exception) {
            Log.e(TAG, "預載入字體時發生未知錯誤", e)
        }
    }
}
