package com.group13.linkap.sync

import android.content.Context

class SyncPreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "linkap_sync_prefs",
        Context.MODE_PRIVATE
    )

    fun getLastSyncTime(): Long = prefs.getLong(KEY_LAST_SYNC_TIME, 0L)

    fun setLastSyncTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_SYNC_TIME, timestamp).apply()
    }

    companion object {
        private const val KEY_LAST_SYNC_TIME = "last_sync_time"
    }
}
