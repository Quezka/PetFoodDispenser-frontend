package it.quezka.petfooddispenser

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val SERVER_IP = stringPreferencesKey("server_ip")
    private val TEST_MODE = androidx.datastore.preferences.core.booleanPreferencesKey("test_mode")
    private val PROLUNGHE_SERBATOIO = intPreferencesKey("prolunghe_serbatoio")
    private val VOLUME_MIN = intPreferencesKey("volume_min")

    val serverIp: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SERVER_IP] ?: ""
        }

    val testMode: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[TEST_MODE] ?: false
        }

    val prolungheSerbatoio: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PROLUNGHE_SERBATOIO] ?: 0
        }

    val volumeMin: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[VOLUME_MIN] ?: 0
        }

    suspend fun updateServerIp(ip: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_IP] = ip
        }
    }

    suspend fun updateTestMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[TEST_MODE] = enabled
        }
    }

    suspend fun updateProlungheSerbatoio(count: Int) {
        context.dataStore.edit { preferences ->
            preferences[PROLUNGHE_SERBATOIO] = count
        }
    }

    suspend fun updateVolumeMin(volume: Int) {
        context.dataStore.edit { preferences ->
            preferences[VOLUME_MIN] = volume
        }
    }
}
