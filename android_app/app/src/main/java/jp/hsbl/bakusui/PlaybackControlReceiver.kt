package jp.hsbl.bakusui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class PlaybackControlReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        if (action == PlaybackBus.ACTION_TOGGLE || action == PlaybackBus.ACTION_STOP) {
            PlaybackBus.listener?.invoke(action)
        }
    }
}

