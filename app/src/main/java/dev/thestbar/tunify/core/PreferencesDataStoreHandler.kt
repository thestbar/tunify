package dev.thestbar.tunify.core

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.thestbar.tunify.data.preferences.ThemePreference
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object PreferencesDataStoreHandler {

    private val CURRENT_TUNING_ID = intPreferencesKey("current_tuning_id")
    private val HAS_BEEN_INITIALIZED = booleanPreferencesKey("has_been_initialized")
    private val IS_TUNER_LOCKED = booleanPreferencesKey("is_tuner_locked")
    private val IS_LOAD_LAST_MUTED_STATE = booleanPreferencesKey("is_load_last_muted_state")
    private val IS_TUNING = booleanPreferencesKey("is_tuning")
    private val THEME_PREFERENCE = stringPreferencesKey("theme_preference")

    fun hasBeenInitialized(context: Context): Flow<Boolean?> =
        context.dataStore.data.map { it[HAS_BEEN_INITIALIZED] }

    suspend fun setHasBeenInitialized(context: Context, value: Boolean) {
        context.dataStore.edit { it[HAS_BEEN_INITIALIZED] = value }
    }

    fun getCurrentTuningId(context: Context): Flow<Int?> =
        context.dataStore.data.map { it[CURRENT_TUNING_ID] }

    suspend fun setCurrentTuningId(context: Context, newId: Int) {
        context.dataStore.edit { it[CURRENT_TUNING_ID] = newId }
    }

    fun getIsTunerLocked(context: Context): Flow<Boolean?> =
        context.dataStore.data.map { it[IS_TUNER_LOCKED] }

    suspend fun setIsTunerLocked(context: Context, value: Boolean) {
        context.dataStore.edit { it[IS_TUNER_LOCKED] = value }
    }

    fun getIsLoadLastMutedState(context: Context): Flow<Boolean?> =
        context.dataStore.data.map { it[IS_LOAD_LAST_MUTED_STATE] }

    suspend fun setIsLoadLastMutedState(context: Context, value: Boolean) {
        context.dataStore.edit { it[IS_LOAD_LAST_MUTED_STATE] = value }
    }

    fun getIsTuning(context: Context): Flow<Boolean?> =
        context.dataStore.data.map { it[IS_TUNING] }

    suspend fun setIsTuning(context: Context, value: Boolean) {
        context.dataStore.edit { it[IS_TUNING] = value }
    }

    fun getThemePreference(context: Context): Flow<ThemePreference> =
        context.dataStore.data.map { prefs ->
            when (prefs[THEME_PREFERENCE]) {
                "LIGHT" -> ThemePreference.LIGHT
                "DARK"  -> ThemePreference.DARK
                else    -> ThemePreference.SYSTEM
            }
        }

    suspend fun setThemePreference(context: Context, value: ThemePreference) {
        context.dataStore.edit { it[THEME_PREFERENCE] = value.name }
    }
}
