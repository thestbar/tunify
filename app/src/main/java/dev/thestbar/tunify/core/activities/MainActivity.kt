package dev.thestbar.tunify.core.activities

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dev.thestbar.tunify.R
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.databinding.MainAppScreenBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainAppScreenBinding

    private var permissionToRecordAccepted = false
    private val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults.isNotEmpty() && requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            permissionToRecordAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        binding = MainAppScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initNavigation()
        initDatabase()
        initSettings()
        askForPermissions()
    }

    private fun initNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        binding.bottomNavigation.setupWithNavController(navController)

        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.fragmentContainerView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, systemBars.top, v.paddingRight, v.paddingBottom)
            insets
        }
    }

    private fun initDatabase() {
        lifecycleScope.launch {
            try {
                val dbHasBeenInitialized = PreferencesDataStoreHandler
                    .hasBeenInitialized(applicationContext)
                    .first() ?: false
                if (!dbHasBeenInitialized) {
                    TuningHandler.resetDatabaseValuesToDefault(TuningRepository(application))
                }
            } catch (e: NullPointerException) {
                Log.w("MainActivity@initDatabase",
                    "NullPointerException when reading hasBeenInitialized; resetting DB")
                TuningHandler.resetDatabaseValuesToDefault(TuningRepository(application))
                PreferencesDataStoreHandler.setHasBeenInitialized(applicationContext, true)
            }
        }
    }

    private fun initSettings() {
        lifecycleScope.launch {
            // IS_TUNER_LOCKED
            try {
                val isTunerLocked = PreferencesDataStoreHandler
                    .getIsTunerLocked(applicationContext)
                    .first() ?: false
                Log.d("IS_TUNER_LOCKED", isTunerLocked.toString())
            } catch (e: NullPointerException) {
                Log.d("IS_TUNER_LOCKED", "Value was not initialized. Set to `false`")
                PreferencesDataStoreHandler.setIsTunerLocked(applicationContext, false)
            }

            // IS_LOAD_LAST_MUTED_STATE
            try {
                val isLoadLastMutedState = PreferencesDataStoreHandler
                    .getIsLoadLastMutedState(applicationContext)
                    .first() ?: false
                Log.d("IS_LOAD_LAST_MUTED_STATE", isLoadLastMutedState.toString())
            } catch (e: NullPointerException) {
                Log.d("IS_LOAD_LAST_MUTED_STATE", "Value was not initialized. Set to `true`")
                PreferencesDataStoreHandler.setIsLoadLastMutedState(applicationContext, true)
            }

            // IS_TUNING
            try {
                val isTuning = PreferencesDataStoreHandler
                    .getIsTuning(applicationContext)
                    .first() ?: false
                Log.d("IS_TUNING", isTuning.toString())
            } catch (e: NullPointerException) {
                Log.d("IS_TUNING", "Value was not initialized. Set to `true`")
                PreferencesDataStoreHandler.setIsTuning(applicationContext, true)
            }
        }
    }

    fun isPermissionToRecordAccepted(): Boolean = permissionToRecordAccepted

    fun askForPermissions() {
        ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}
