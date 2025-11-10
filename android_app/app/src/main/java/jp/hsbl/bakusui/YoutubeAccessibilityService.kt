package jp.hsbl.bakusui

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.content.Context

private const val YT_PKG = "com.google.android.youtube"

class YoutubeAccessibilityService : AccessibilityService() {
    private val prefs by lazy { getSharedPreferences("bakusui_prefs", Context.MODE_PRIVATE) }

    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg != YT_PKG) return

        val cls = event.className?.toString() ?: ""
        val now = System.currentTimeMillis()

        var isShorts = false
        // 1) クラス名ヒューリスティクス
        if (cls.contains("Short", true) || cls.contains("Shorts", true)) {
            isShorts = true
        } else {
            // 2) ビューツリー内の文言にShort/ショートが含まれるか
            try {
                val root: AccessibilityNodeInfo? = rootInActiveWindow
                if (root != null) {
                    val txt = collectText(root)
                    if (txt.contains("Short", true) || txt.contains("ショート")) {
                        isShorts = true
                    }
                }
            } catch (_: Throwable) { }
        }

        // 保存
        try {
            prefs.edit()
                .putBoolean("yt_is_shorts", isShorts)
                .putLong("yt_last_event", now)
                .putString("yt_last_class", cls)
                .apply()
        } catch (_: Throwable) { }
    }

    override fun onInterrupt() {
    }

    private fun collectText(node: AccessibilityNodeInfo): String {
        val sb = StringBuilder()
        fun dfs(n: AccessibilityNodeInfo?) {
            if (n == null) return
            n.text?.let { sb.append(it).append(' ') }
            for (i in 0 until n.childCount) {
                dfs(n.getChild(i))
            }
        }
        dfs(node)
        return sb.toString()
    }
}


