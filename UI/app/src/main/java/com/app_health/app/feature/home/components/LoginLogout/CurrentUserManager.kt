package com.app_health.app.feature.home.components.LoginLogout


import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.currentSessionDataStore by preferencesDataStore(name = "current_session_prefs")

class CurrentUserManager(private val context: Context) {
    companion object {
        private val KEY_CURRENT_EMAIL = stringPreferencesKey("key_current_email")
    }

    // Lưu email của tài khoản đang đăng nhập
    suspend fun saveCurrentEmail(email: String) {
        context.currentSessionDataStore.edit { prefs ->
            prefs[KEY_CURRENT_EMAIL] = email
        }
    }

    // Lấy email hiện tại dưới dạng Flow để quan sát
    val currentEmailFlow: Flow<String?> = context.currentSessionDataStore.data.map { prefs ->
        prefs[KEY_CURRENT_EMAIL]
    }

    // Đăng xuất (xóa phiên)
    suspend fun clearSession() {
        context.currentSessionDataStore.edit { prefs ->
            prefs.clear()
        }
    }
}