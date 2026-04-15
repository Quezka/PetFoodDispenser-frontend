package it.quezka.petfooddispenser

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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

    val serverIp: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[SERVER_IP] ?: ""
        }

    suspend fun updateServerIp(ip: String) {
        context.dataStore.edit { preferences ->
            preferences[SERVER_IP] = ip
        }
    }
}
