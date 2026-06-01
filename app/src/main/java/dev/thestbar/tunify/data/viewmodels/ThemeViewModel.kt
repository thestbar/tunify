package dev.thestbar.tunify.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.data.preferences.ThemePreference
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext

    val themePreference: StateFlow<ThemePreference> =
        PreferencesDataStoreHandler.getThemePreference(context)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), ThemePreference.SYSTEM)

    val isTunerLocked: StateFlow<Boolean> =
        PreferencesDataStoreHandler.getIsTunerLocked(context)
            .map { it ?: false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    val isLoadLastMutedState: StateFlow<Boolean> =
        PreferencesDataStoreHandler.getIsLoadLastMutedState(context)
            .map { it ?: false }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), false)

    fun setThemePreference(pref: ThemePreference) {
        viewModelScope.launch { PreferencesDataStoreHandler.setThemePreference(context, pref) }
    }

    fun setIsTunerLocked(value: Boolean) {
        viewModelScope.launch { PreferencesDataStoreHandler.setIsTunerLocked(context, value) }
    }

    fun setIsLoadLastMutedState(value: Boolean) {
        viewModelScope.launch { PreferencesDataStoreHandler.setIsLoadLastMutedState(context, value) }
    }
}
