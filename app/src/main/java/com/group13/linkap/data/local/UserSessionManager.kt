package com.group13.linkap.data.local

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserSessionManager(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(
        "linkap_user_session",
        Context.MODE_PRIVATE
    )

    private val _barangayIdFlow = MutableStateFlow(getBarangayId())
    val barangayIdFlow: StateFlow<String> = _barangayIdFlow.asStateFlow()

    fun saveSession(uid: String, barangayId: String) {
        prefs.edit()
            .putString(KEY_UID, uid)
            .putString(KEY_BARANGAY_ID, barangayId)
            .commit()
        _barangayIdFlow.value = barangayId
    }

    fun getUid(): String? {
        return prefs.getString(KEY_UID, null)
    }

    fun getBarangayId(): String {
        return prefs.getString(KEY_BARANGAY_ID, "") ?: ""
    }

    fun isLegacyBackfilled(): Boolean {
        return prefs.getBoolean(KEY_LEGACY_BACKFILLED, false)
    }

    fun setLegacyBackfilled() {
        prefs.edit().putBoolean(KEY_LEGACY_BACKFILLED, true).commit()
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_UID)
            .remove(KEY_BARANGAY_ID)
            .commit()
        _barangayIdFlow.value = ""
    }

    companion object {
        private const val KEY_UID = "active_uid"
        private const val KEY_BARANGAY_ID = "active_barangay_id"
        private const val KEY_LEGACY_BACKFILLED = "is_legacy_backfilled"
    }
}
