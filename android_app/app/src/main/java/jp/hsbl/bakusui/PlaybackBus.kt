package jp.hsbl.bakusui

object PlaybackBus {
    @Volatile var listener: ((String) -> Unit)? = null
    const val ACTION_TOGGLE = "jp.hsbl.bakusui.action.TOGGLE"
    const val ACTION_STOP = "jp.hsbl.bakusui.action.STOP"
}

