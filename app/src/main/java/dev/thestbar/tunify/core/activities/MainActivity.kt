package dev.thestbar.tunify.core.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.data.viewmodels.ThemeViewModel
import dev.thestbar.tunify.data.viewmodels.TunerViewModel
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.ui.navigation.TunifyNavHost
import dev.thestbar.tunify.ui.theme.TunifyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val themeViewModel: ThemeViewModel by viewModels()
    private val tunerViewModel: TunerViewModel by viewModels()
    private val tuningViewModel: TuningViewModel by viewModels()

    private val requestPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) tunerViewModel.onPermissionGranted()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        initDatabase()
        requestPermission.launch(android.Manifest.permission.RECORD_AUDIO)

        setContent {
            val themePreference by themeViewModel.themePreference.collectAsStateWithLifecycle()
            TunifyTheme(themePreference = themePreference) {
                TunifyNavHost(
                    tunerViewModel = tunerViewModel,
                    tuningViewModel = tuningViewModel,
                    themeViewModel = themeViewModel
                )
            }
        }
    }

    private fun initDatabase() {
        lifecycleScope.launch {
            try {
                val initialized = PreferencesDataStoreHandler
                    .hasBeenInitialized(applicationContext).first() ?: false
                if (!initialized) {
                    TuningHandler.resetDatabaseValuesToDefault(TuningRepository(application))
                    PreferencesDataStoreHandler.setHasBeenInitialized(applicationContext, true)
                }
            } catch (e: NullPointerException) {
                TuningHandler.resetDatabaseValuesToDefault(TuningRepository(application))
                PreferencesDataStoreHandler.setHasBeenInitialized(applicationContext, true)
            }
        }
    }
}
