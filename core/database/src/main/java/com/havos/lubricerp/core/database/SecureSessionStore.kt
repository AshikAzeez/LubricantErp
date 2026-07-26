package com.havos.lubricerp.core.database

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.havos.lubricerp.core.common.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

private val Context.secureDataStore by preferencesDataStore(name = "goal_erp_secure_store")

data class SessionData(
    val username: String,
    val token: String,
    val refreshToken: String = ""
)

interface SecureSessionStore {
    val sessionFlow: Flow<SessionData?>
    val rememberedUsernameFlow: Flow<String>
    val rememberMeEnabledFlow: Flow<Boolean>
    val themeModeFlow: Flow<ThemeMode>
    val salesFilterFlow: Flow<Pair<String, String>?>

    suspend fun saveSession(sessionData: SessionData)
    suspend fun saveRememberedUsername(username: String)
    suspend fun clearRememberedUsername()
    suspend fun setRememberMeEnabled(enabled: Boolean)
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun saveSalesFilter(fromDate: String, toDate: String)
    suspend fun clearSalesFilter()

    suspend fun clearSession()
}

class SecureSessionStoreImpl(
    context: Context,
    private val cryptoManager: CryptoManager
) : SecureSessionStore {

    private val datastore = context.secureDataStore

    override val sessionFlow: Flow<SessionData?> = datastore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            val encryptedToken = preferences[Keys.TOKEN] ?: return@map null
            val encryptedUser = preferences[Keys.USERNAME] ?: return@map null
            val encryptedRefresh = preferences[Keys.REFRESH_TOKEN] ?: ""
            try {
                SessionData(
                    username = cryptoManager.decrypt(encryptedUser),
                    token = cryptoManager.decrypt(encryptedToken),
                    refreshToken = if (encryptedRefresh.isNotBlank()) cryptoManager.decrypt(encryptedRefresh) else ""
                )
            } catch (e: Exception) {
                null
            }
        }
        .flowOn(Dispatchers.IO)

    override val rememberedUsernameFlow: Flow<String> = datastore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            val encryptedUsername = preferences[Keys.REMEMBERED_USERNAME] ?: return@map ""
            try {
                cryptoManager.decrypt(encryptedUsername)
            } catch (_: Exception) {
                ""
            }
        }
        .flowOn(Dispatchers.IO)

    override val rememberMeEnabledFlow: Flow<Boolean> = datastore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[Keys.REMEMBER_ME_ENABLED] ?: false }
        .flowOn(Dispatchers.IO)

    override val themeModeFlow: Flow<ThemeMode> = datastore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            val encryptedValue = preferences[Keys.THEME_MODE] ?: return@map ThemeMode.SYSTEM
            try {
                ThemeMode.from(cryptoManager.decrypt(encryptedValue))
            } catch (_: Exception) {
                ThemeMode.SYSTEM
            }
        }
        .flowOn(Dispatchers.IO)

    override val salesFilterFlow: Flow<Pair<String, String>?> = datastore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences ->
            val from = preferences[Keys.SALES_FILTER_FROM] ?: return@map null
            val to = preferences[Keys.SALES_FILTER_TO] ?: return@map null
            Pair(from, to)
        }
        .flowOn(Dispatchers.IO)

    override suspend fun saveSession(sessionData: SessionData) {
        withContext(Dispatchers.IO) {
            datastore.edit { preferences ->
                preferences[Keys.USERNAME] = cryptoManager.encrypt(sessionData.username)
                preferences[Keys.TOKEN] = cryptoManager.encrypt(sessionData.token)
                if (sessionData.refreshToken.isNotBlank()) {
                    preferences[Keys.REFRESH_TOKEN] = cryptoManager.encrypt(sessionData.refreshToken)
                }
            }
        }
    }

    override suspend fun saveRememberedUsername(username: String) {
        withContext(Dispatchers.IO) {
            datastore.edit { preferences ->
                if (username.isBlank()) {
                    preferences.remove(Keys.REMEMBERED_USERNAME)
                } else {
                    preferences[Keys.REMEMBERED_USERNAME] = cryptoManager.encrypt(username)
                }
            }
        }
    }

    override suspend fun clearRememberedUsername() {
        withContext(Dispatchers.IO) {
            datastore.edit { preferences ->
                preferences.remove(Keys.REMEMBERED_USERNAME)
            }
        }
    }

    override suspend fun setRememberMeEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            datastore.edit { preferences ->
                preferences[Keys.REMEMBER_ME_ENABLED] = enabled
            }
        }
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        withContext(Dispatchers.IO) {
            datastore.edit { preferences ->
                preferences[Keys.THEME_MODE] = cryptoManager.encrypt(themeMode.name)
            }
        }
    }

    override suspend fun saveSalesFilter(fromDate: String, toDate: String) {
        withContext(Dispatchers.IO) {
            datastore.edit { preferences ->
                preferences[Keys.SALES_FILTER_FROM] = fromDate
                preferences[Keys.SALES_FILTER_TO] = toDate
            }
        }
    }

    override suspend fun clearSalesFilter() {
        withContext(Dispatchers.IO) {
            datastore.edit { preferences ->
                preferences.remove(Keys.SALES_FILTER_FROM)
                preferences.remove(Keys.SALES_FILTER_TO)
            }
        }
    }

    override suspend fun clearSession() {
        withContext(Dispatchers.IO) {
            datastore.edit { preferences ->
                preferences.remove(Keys.USERNAME)
                preferences.remove(Keys.TOKEN)
                preferences.remove(Keys.REFRESH_TOKEN)
                preferences.remove(Keys.SALES_FILTER_FROM)
                preferences.remove(Keys.SALES_FILTER_TO)
            }
        }
    }

    private object Keys {
        val USERNAME = stringPreferencesKey("username")
        val TOKEN = stringPreferencesKey("token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val REMEMBERED_USERNAME = stringPreferencesKey("remembered_username")
        val REMEMBER_ME_ENABLED = booleanPreferencesKey("remember_me_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val SALES_FILTER_FROM = stringPreferencesKey("sales_filter_from")
        val SALES_FILTER_TO = stringPreferencesKey("sales_filter_to")
    }
}
