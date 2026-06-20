# Compose Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace all Fragments and XML layouts with idiomatic Jetpack Compose screens, add Material3 dark + light theme with custom palette, and add a System / Light / Dark theme-preference setting.

**Architecture:** Full rip-and-replace on `feat/compose-migration`. Two new ViewModels (`TunerViewModel`, `ThemeViewModel`), a new theme layer (`Color.kt`, `TunifyTheme.kt`), four screen composables, `TunifyNavHost` with a `Scaffold` bottom bar, and a refactored `MainActivity` that calls `setContent`. Existing `TuningViewModel` is unchanged except for one new `resetToDefaults()` method.

**Tech Stack:** Jetpack Compose (BOM 2024.09.03), Material3, Navigation Compose 2.8.9, `collectAsStateWithLifecycle`, Room 2.8.4, DataStore Preferences, Kotlin Coroutines/Flow.

---

## File Map

| Action | Path |
|--------|------|
| Modify | `gradle/libs.versions.toml` |
| Modify | `app/build.gradle.kts` |
| Create | `ui/theme/Color.kt` |
| Create | `ui/theme/TunifyTheme.kt` |
| Create | `data/preferences/ThemePreference.kt` |
| Modify | `core/PreferencesDataStoreHandler.kt` |
| Create | `data/viewmodels/ThemeViewModel.kt` |
| Modify | `core/RecordingRunnable.kt` |
| Create | `data/viewmodels/TunerViewModel.kt` |
| Modify | `data/viewmodels/TuningViewModel.kt` |
| Create | `ui/screens/InfoScreen.kt` |
| Create | `ui/screens/SettingsScreen.kt` |
| Create | `ui/screens/TunerScreen.kt` |
| Create | `ui/screens/TuningsScreen.kt` |
| Create | `ui/navigation/TunifyNavHost.kt` |
| Modify | `core/activities/MainActivity.kt` |
| Delete | All Fragment classes (MainFragment, TuningsFragment, SettingsFragment, InfoFragment, AddTuningDialogFragment) |
| Delete | `core/TuningAdapter.kt` |
| Delete | All XML layouts, nav_graph.xml, bottom_navigation_menu.xml |
| Delete | Unused XML drawables |

All paths are relative to `app/src/main/java/dev/thestbar/tunify/` for Kotlin files and `app/src/main/res/` for resources.

---

## Task 1: Add Compose Dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Add version and library entries to libs.versions.toml**

Replace the entire file content:

```toml
[versions]
agp = "8.7.0"
kotlin = "2.1.21"
appcompat = "1.7.1"
material = "1.13.0"
constraintlayout = "2.2.1"
room = "2.8.4"
datastorePreferences = "1.2.1"
junit = "4.13.2"
androidxJunit = "1.2.1"
espresso = "3.6.1"
jtransforms = "3.1"
speedviewlib = "1.6.1"
ksp = "2.1.21-2.0.2"
coroutines = "1.10.2"
lifecycle = "2.9.1"
fragmentKtx = "1.8.6"
activityKtx = "1.10.1"
navigation = "2.8.9"
composeBom = "2024.09.03"
activityCompose = "1.10.1"

[libraries]
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxJunit" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-guava = { group = "androidx.room", name = "room-guava", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
room-paging = { group = "androidx.room", name = "room-paging", version.ref = "room" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }
jtransforms = { group = "com.github.wendykierp", name = "JTransforms", version.ref = "jtransforms" }
speedviewlib = { group = "com.github.anastr", name = "speedviewlib", version.ref = "speedviewlib" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-compose = { group = "androidx.lifecycle", name = "lifecycle-runtime-compose", version.ref = "lifecycle" }
androidx-fragment-ktx = { group = "androidx.fragment", name = "fragment-ktx", version.ref = "fragmentKtx" }
androidx-activity-ktx = { group = "androidx.activity", name = "activity-ktx", version.ref = "activityKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-navigation-fragment-ktx = { group = "androidx.navigation", name = "navigation-fragment-ktx", version.ref = "navigation" }
androidx-navigation-ui-ktx = { group = "androidx.navigation", name = "navigation-ui-ktx", version.ref = "navigation" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```

- [ ] **Step 2: Update app/build.gradle.kts**

Replace the entire file:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.parcelize")
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.thestbar.tunify"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.thestbar.tunify"
        minSdk = 24
        targetSdk = 36
        versionCode = 3
        versionName = "2.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    buildFeatures {
        viewBinding = true
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.kotlinx.coroutines.android)
    // Compose BOM
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.extended)
    debugImplementation(libs.compose.ui.tooling)
    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.guava)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)
    // DataStore
    implementation(libs.datastore.preferences)
    // Audio DSP
    implementation(libs.jtransforms)
    implementation(libs.speedviewlib)
    // Test
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
```

- [ ] **Step 3: Sync Gradle and verify the project builds**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. If dependency resolution fails, update `composeBom` to the latest stable version from https://developer.android.com/jetpack/compose/bom/bom-mapping.

- [ ] **Step 4: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts
git commit -m "feat: add Jetpack Compose dependencies"
```

---

## Task 2: Theme Layer (Color.kt + TunifyTheme.kt)

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/ui/theme/Color.kt`
- Create: `app/src/main/java/dev/thestbar/tunify/ui/theme/TunifyTheme.kt`

- [ ] **Step 1: Create Color.kt**

```kotlin
package dev.thestbar.tunify.ui.theme

import androidx.compose.ui.graphics.Color

val RaisinBlack   = Color(0xFF231F20)
val Linen         = Color(0xFFEFE6DD)
val Wenge         = Color(0xFF564D4E)
val PersianRed    = Color(0xFFBB4430)
val Verdigris     = Color(0xFF7EBDC2)
val TaupeGray     = Color(0xFF8C7D7F)
val Vanilla       = Color(0xFFF3DFA2)
val LinenWengeMix = Color(0xFFD4C5BF)
```

- [ ] **Step 2: Create TunifyTheme.kt**

Note: `ThemePreference` is created in Task 3. For now the composable accepts a `Boolean darkTheme` parameter; it will be updated in Task 3 to accept `ThemePreference`.

```kotlin
package dev.thestbar.tunify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColors = darkColorScheme(
    primary            = PersianRed,
    onPrimary          = Linen,
    secondary          = Verdigris,
    onSecondary        = RaisinBlack,
    tertiary           = Vanilla,
    background         = RaisinBlack,
    onBackground       = Linen,
    surface            = Wenge,
    onSurface          = Linen,
    onSurfaceVariant   = TaupeGray,
    surfaceContainer   = Wenge,
)

private val LightColors = lightColorScheme(
    primary            = PersianRed,
    onPrimary          = Linen,
    secondary          = Verdigris,
    onSecondary        = RaisinBlack,
    tertiary           = Vanilla,
    background         = Linen,
    onBackground       = RaisinBlack,
    surface            = LinenWengeMix,
    onSurface          = RaisinBlack,
    onSurfaceVariant   = Wenge,
    surfaceContainer   = LinenWengeMix,
)

@Composable
fun TunifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
```

- [ ] **Step 3: Build to verify the new files compile**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/ui/
git commit -m "feat: add TunifyTheme with Material3 dark and light color schemes"
```

---

## Task 3: ThemePreference + DataStore Key + ThemeViewModel

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/data/preferences/ThemePreference.kt`
- Modify: `app/src/main/java/dev/thestbar/tunify/core/PreferencesDataStoreHandler.kt`
- Create: `app/src/main/java/dev/thestbar/tunify/data/viewmodels/ThemeViewModel.kt`
- Modify: `app/src/main/java/dev/thestbar/tunify/ui/theme/TunifyTheme.kt`

- [ ] **Step 1: Create ThemePreference.kt**

```kotlin
package dev.thestbar.tunify.data.preferences

enum class ThemePreference { SYSTEM, LIGHT, DARK }
```

- [ ] **Step 2: Add THEME_PREFERENCE key to PreferencesDataStoreHandler.kt**

Add this import at the top of the file:

```kotlin
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.thestbar.tunify.data.preferences.ThemePreference
```

Add inside the `object PreferencesDataStoreHandler` block, after the last existing key constant:

```kotlin
private val THEME_PREFERENCE = stringPreferencesKey("theme_preference")

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
```

- [ ] **Step 3: Create ThemeViewModel.kt**

```kotlin
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
```

- [ ] **Step 4: Update TunifyTheme.kt to accept ThemePreference**

Replace the existing `TunifyTheme` composable signature:

```kotlin
package dev.thestbar.tunify.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import dev.thestbar.tunify.data.preferences.ThemePreference

private val DarkColors = darkColorScheme(
    primary            = PersianRed,
    onPrimary          = Linen,
    secondary          = Verdigris,
    onSecondary        = RaisinBlack,
    tertiary           = Vanilla,
    background         = RaisinBlack,
    onBackground       = Linen,
    surface            = Wenge,
    onSurface          = Linen,
    onSurfaceVariant   = TaupeGray,
    surfaceContainer   = Wenge,
)

private val LightColors = lightColorScheme(
    primary            = PersianRed,
    onPrimary          = Linen,
    secondary          = Verdigris,
    onSecondary        = RaisinBlack,
    tertiary           = Vanilla,
    background         = Linen,
    onBackground       = RaisinBlack,
    surface            = LinenWengeMix,
    onSurface          = RaisinBlack,
    onSurfaceVariant   = Wenge,
    surfaceContainer   = LinenWengeMix,
)

@Composable
fun TunifyTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.DARK   -> true
        ThemePreference.LIGHT  -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
```

- [ ] **Step 5: Build to verify**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/
git commit -m "feat: add ThemePreference, DataStore key, and ThemeViewModel"
```

---

## Task 4: Refactor RecordingRunnable + Create TunerViewModel

The current `RecordingRunnable` holds `TextView` and `SpeedView` references. In Compose these Views don't exist — instead the runnable will call a lambda to push pitch updates into ViewModel state.

**Files:**
- Modify: `app/src/main/java/dev/thestbar/tunify/core/RecordingRunnable.kt`
- Create: `app/src/main/java/dev/thestbar/tunify/data/viewmodels/TunerViewModel.kt`

- [ ] **Step 1: Refactor RecordingRunnable to use a callback instead of Views**

Replace the entire file:

```kotlin
package dev.thestbar.tunify.core

import android.media.AudioRecord
import dev.thestbar.tunify.util.algorithms.NoteDetection
import dev.thestbar.tunify.util.algorithms.Yin
import dev.thestbar.tunify.util.notes.NotesStructure
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext
import kotlin.math.roundToLong

class RecordingRunnable(
    private val recorder: AudioRecord,
    private val inputBuffer: ShortArray,
    private val onPitchDetected: (note: String, centsOffset: Float) -> Unit
) {

    private val yinInstance = Yin(recorder.sampleRate.toDouble())

    init {
        if (noteDetection == null) {
            noteDetection = NoteDetection(NotesStructure.allNotes)
        }
    }

    suspend fun record() {
        val len = inputBuffer.size
        while (currentCoroutineContext().isActive) {
            recorder.read(inputBuffer, 0, len)
            val pitchInHz = yinInstance.getPitch(inputBuffer)
            if (!pitchInHz.isFinite() || pitchInHz == -1.0) continue
            val nd = noteDetection ?: continue
            val closestNote = nd.findClosestNote(pitchInHz)
            val deltaInCents = NoteDetection.getDifferentInCents(closestNote, pitchInHz)
            withContext(Dispatchers.Main) {
                onPitchDetected(closestNote.name, deltaInCents.roundToLong().toFloat())
            }
        }
    }

    companion object {
        @Volatile private var noteDetection: NoteDetection? = null

        @Synchronized
        fun setNoteDetection(newNoteDetection: NoteDetection) {
            noteDetection = newNoteDetection
        }
    }
}
```

- [ ] **Step 2: Create TunerViewModel.kt**

```kotlin
package dev.thestbar.tunify.data.viewmodels

import android.app.Application
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.core.RecordingRunnable
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.util.algorithms.NoteDetection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TunerUiState(
    val detectedNote: String = "",
    val centsOffset: Float = 0f,
    val isTuning: Boolean = false,
    val currentTuning: Tuning = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]"),
    val currentTuningStrings: List<String> = listOf("E2", "A2", "D3", "G3", "B3", "E4"),
    val hasAudioPermission: Boolean = false
)

class TunerViewModel(application: Application) : AndroidViewModel(application) {

    private val context = application.applicationContext
    private val tuningRepository = TuningRepository(application)

    private val _uiState = MutableStateFlow(TunerUiState())
    val uiState: StateFlow<TunerUiState> = _uiState.asStateFlow()

    val selectedTuningId: StateFlow<Int> =
        PreferencesDataStoreHandler.getCurrentTuningId(context)
            .map { it ?: -1 }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000L), -1)

    private var recorder: AudioRecord? = null
    private var recordingJob: Job? = null

    init {
        viewModelScope.launch { loadInitialState() }
        observeCurrentTuning()
    }

    private fun observeCurrentTuning() {
        viewModelScope.launch {
            selectedTuningId
                .flatMapLatest { id -> tuningRepository.getTuningById(id) }
                .collect { tuning ->
                    val resolved = tuning ?: Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]")
                    val guitarTuning = TuningHandler.getGuitarTuningFromTuning(resolved)
                    val isTunerLocked = try {
                        PreferencesDataStoreHandler.getIsTunerLocked(context).first() ?: false
                    } catch (e: NullPointerException) { false }
                    if (isTunerLocked) {
                        RecordingRunnable.setNoteDetection(NoteDetection(guitarTuning.notes))
                    }
                    _uiState.update { it.copy(
                        currentTuning = resolved,
                        currentTuningStrings = guitarTuning.notes.map { n -> n.name }
                    ) }
                }
        }
    }

    private suspend fun loadInitialState() {
        val hasPermission = ActivityCompat.checkSelfPermission(
            context, android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val isLoadLastMutedState = try {
            PreferencesDataStoreHandler.getIsLoadLastMutedState(context).first() ?: false
        } catch (e: NullPointerException) { false }

        val isTuning = try {
            PreferencesDataStoreHandler.getIsTuning(context).first() ?: false
        } catch (e: NullPointerException) { false }

        _uiState.update { it.copy(hasAudioPermission = hasPermission) }

        if (hasPermission && isLoadLastMutedState && isTuning) {
            startRecording()
        }
    }

    fun setIsTuning(value: Boolean) {
        viewModelScope.launch {
            PreferencesDataStoreHandler.setIsTuning(context, value)
        }
        if (value) startRecording() else stopRecording()
    }

    fun selectTuning(tuningId: Int) {
        viewModelScope.launch {
            PreferencesDataStoreHandler.setCurrentTuningId(context, tuningId)
        }
    }

    fun onPermissionGranted() {
        _uiState.update { it.copy(hasAudioPermission = true) }
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(
                context, android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        val buf = ShortArray(BUFFER_SIZE)
        val rec = AudioRecord(
            MediaRecorder.AudioSource.UNPROCESSED,
            SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        )
        recorder = rec
        rec.startRecording()
        _uiState.update { it.copy(isTuning = true) }

        val runnable = RecordingRunnable(rec, buf) { note, cents ->
            _uiState.update { it.copy(detectedNote = note, centsOffset = cents) }
        }
        recordingJob = viewModelScope.launch(Dispatchers.IO) {
            runnable.record()
        }
    }

    private fun stopRecording() {
        recordingJob?.cancel()
        recordingJob = null
        recorder?.stop()
        recorder?.release()
        recorder = null
        _uiState.update { it.copy(isTuning = false, detectedNote = "", centsOffset = 0f) }
    }

    override fun onCleared() {
        super.onCleared()
        stopRecording()
    }

    companion object {
        const val NEEDLE_ANIMATION_SPEED = 300L
        private const val SAMPLING_RATE_IN_HZ = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 4
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT
        ) * BUFFER_SIZE_FACTOR
    }
}
```

- [ ] **Step 3: Build to verify (MainFragment will still compile since it doesn't use the new callback-based constructor yet)**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. `MainFragment` will have a compile error because it still creates `RecordingRunnable(rec, binding.textViewPitch, binding.speedView, buf)` — the old 4-param constructor. That's acceptable; it will be fixed when MainFragment is deleted in Task 12.

If the build fails **only** because of `MainFragment`, that is expected. If other files fail, fix them before continuing.

> **Note:** The build will fail in `MainFragment.kt` because `RecordingRunnable`'s constructor signature changed. To keep the build green while old fragments still exist, temporarily comment out the `RecordingRunnable` instantiation line in `MainFragment.startRecording()` and add a placeholder:
>
> ```kotlin
> // val runnable = RecordingRunnable(rec, binding.textViewPitch, binding.speedView, buf)
> // recordingJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) { runnable.record() }
> ```
>
> This silences the compile error. The fragment will still exist but the tuner won't work until Task 12 deletes it.

- [ ] **Step 4: Build to verify**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/core/RecordingRunnable.kt \
        app/src/main/java/dev/thestbar/tunify/data/viewmodels/TunerViewModel.kt \
        app/src/main/java/dev/thestbar/tunify/core/fragments/MainFragment.kt
git commit -m "feat: add TunerViewModel, refactor RecordingRunnable to use pitch callback"
```

---

## Task 5: Add resetToDefaults() to TuningViewModel

**Files:**
- Modify: `app/src/main/java/dev/thestbar/tunify/data/viewmodels/TuningViewModel.kt`

- [ ] **Step 1: Add import and method to TuningViewModel**

Add import at the top:
```kotlin
import dev.thestbar.tunify.data.TuningHandler
```

Add method at the end of the class body (before the closing `}`):

```kotlin
fun resetToDefaults() {
    viewModelScope.launch { TuningHandler.resetDatabaseValuesToDefault(repository) }
}
```

- [ ] **Step 2: Build**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/data/viewmodels/TuningViewModel.kt
git commit -m "feat: add TuningViewModel.resetToDefaults()"
```

---

## Task 6: InfoScreen

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/ui/screens/InfoScreen.kt`

- [ ] **Step 1: Create InfoScreen.kt**

This screen renders the existing `R.string.info_str` HTML via an `AndroidView`-wrapped `TextView`. Task #46 will rewrite this content — for now the goal is a functional Compose screen.

```kotlin
package dev.thestbar.tunify.ui.screens

import android.os.Build
import android.text.Html
import android.text.method.LinkMovementMethod
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import dev.thestbar.tunify.R

@Composable
fun InfoScreen() {
    val ctx = LocalContext.current
    val textColor = MaterialTheme.colorScheme.onBackground.toArgb()
    val linkColor = MaterialTheme.colorScheme.primary.toArgb()

    AndroidView(
        factory = { context ->
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                textSize = 14f
                setPadding(48, 48, 48, 48)
                setTextColor(textColor)
                setLinkTextColor(linkColor)
            }
        },
        update = { tv ->
            val raw = ctx.getString(R.string.info_str)
            tv.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(raw)
            }
            tv.setTextColor(textColor)
            tv.setLinkTextColor(linkColor)
        },
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    )
}
```

- [ ] **Step 2: Build**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/ui/screens/InfoScreen.kt
git commit -m "feat: add InfoScreen composable"
```

---

## Task 7: SettingsScreen

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: Create SettingsScreen.kt**

```kotlin
package dev.thestbar.tunify.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.thestbar.tunify.data.preferences.ThemePreference

@Composable
fun SettingsScreen(
    themePreference: ThemePreference,
    isTunerLocked: Boolean,
    isLoadLastMutedState: Boolean,
    onThemeChange: (ThemePreference) -> Unit,
    onTunerLockedChange: (Boolean) -> Unit,
    onLoadLastMutedStateChange: (Boolean) -> Unit,
    onResetDatabase: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset Tuning Database") },
            text = { Text("All your changes will be lost. The database will be reset to the default tunings. Proceed?") },
            confirmButton = {
                TextButton(onClick = { onResetDatabase(); showResetDialog = false }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("No") }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Theme", style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp))

        val themeOptions = listOf(
            ThemePreference.SYSTEM to "System",
            ThemePreference.LIGHT  to "Light",
            ThemePreference.DARK   to "Dark"
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            themeOptions.forEachIndexed { index, (pref, label) ->
                SegmentedButton(
                    selected = themePreference == pref,
                    onClick = { onThemeChange(pref) },
                    shape = SegmentedButtonDefaults.itemShape(index, themeOptions.size),
                    label = { Text(label) }
                )
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Text("Tuner", style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp))

        PreferenceRow(
            label = "Lock Tuner",
            description = "Restrict pitch detection to the selected tuning's strings only",
            checked = isTunerLocked,
            onCheckedChange = onTunerLockedChange
        )

        PreferenceRow(
            label = "Load Last Muted State",
            description = "Resume the tuner in the same muted/active state on next launch",
            checked = isLoadLastMutedState,
            onCheckedChange = onLoadLastMutedStateChange
        )

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        Text("Database", style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp))

        TextButton(onClick = { showResetDialog = true }) {
            Text("Reset Tunings to Default",
                color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun PreferenceRow(
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 16.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
```

- [ ] **Step 2: Build**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/ui/screens/SettingsScreen.kt
git commit -m "feat: add SettingsScreen composable with theme segmented button"
```

---

## Task 8: TunerScreen

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/ui/screens/TunerScreen.kt`

- [ ] **Step 1: Create TunerScreen.kt**

```kotlin
package dev.thestbar.tunify.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.github.anastr.speedviewlib.SpeedView
import com.github.anastr.speedviewlib.components.Section
import com.github.anastr.speedviewlib.components.Style
import dev.thestbar.tunify.R
import dev.thestbar.tunify.data.viewmodels.TunerUiState
import dev.thestbar.tunify.data.viewmodels.TunerViewModel

@Composable
fun TunerScreen(
    state: TunerUiState,
    onToggleTuning: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Tuning toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.isTuning) {
                Button(
                    onClick = onToggleTuning,
                    modifier = Modifier.weight(1f)
                ) { Text("Tuning") }
            } else {
                OutlinedButton(
                    onClick = onToggleTuning,
                    modifier = Modifier.weight(1f)
                ) { Text("Muted") }
            }
        }

        // Detected note badge
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "DETECTED NOTE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.detectedNote.ifEmpty { "—" },
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                val centsText = when {
                    state.detectedNote.isEmpty() -> ""
                    state.centsOffset > 0  -> "+${state.centsOffset.toInt()} ¢ sharp"
                    state.centsOffset < 0  -> "${state.centsOffset.toInt()} ¢ flat"
                    else                   -> "in tune"
                }
                Text(
                    text = centsText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Speedometer
        SpeedViewComposable(
            centsOffset = state.centsOffset,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        // Current tuning + string chips
        if (state.currentTuningStrings.isNotEmpty()) {
            Text(
                text = state.currentTuning.name,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(state.currentTuningStrings) { note ->
                    SuggestionChip(
                        onClick = {},
                        label = { Text(note) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SpeedViewComposable(
    centsOffset: Float,
    modifier: Modifier = Modifier
) {
    val ctx = LocalContext.current
    // Capture vanilla color outside AndroidView factory
    val sectionColor = remember { ContextCompat.getColor(ctx, R.color.custom_vanilla) }

    AndroidView(
        factory = { context ->
            SpeedView(context).apply {
                minSpeed = -50f
                maxSpeed = 50f
                clearSections()
                val section = Section(0f, 1f, sectionColor)
                section.style = Style.ROUND
                addSections(section)
                speedometerWidth = 8f
                marksNumber = 9
                markStyle = Style.ROUND
                marksPadding = 5f
                markHeight = 10f
                tickNumber = 11
                tickPadding = 20f
            }
        },
        update = { sv ->
            sv.speedTo(centsOffset, TunerViewModel.NEEDLE_ANIMATION_SPEED)
        },
        modifier = modifier
    )
}
```

- [ ] **Step 2: Build**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/ui/screens/TunerScreen.kt
git commit -m "feat: add TunerScreen composable with SpeedView AndroidView wrapper"
```

---

## Task 9: TuningsScreen

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/ui/screens/TuningsScreen.kt`

- [ ] **Step 1: Create TuningsScreen.kt**

```kotlin
package dev.thestbar.tunify.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.data.viewmodels.SortOrder
import dev.thestbar.tunify.util.notes.NotesStructure
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TuningsScreen(
    tunings: List<Tuning>,
    selectedTuningId: Int,
    sortOrder: SortOrder,
    onSearchQueryChange: (String) -> Unit,
    onSortOrderChange: (SortOrder) -> Unit,
    onSelectTuning: (Int) -> Unit,
    onDeleteTuning: (Tuning) -> Unit,
    onSaveTuning: (Tuning) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val listState = rememberLazyListState()
    val isFabExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }

    var searchQuery by remember { mutableStateOf("") }
    var searchActive by remember { mutableStateOf(false) }
    var bottomSheetTuning by remember { mutableStateOf<Tuning?>(null) }
    var showAddSheet by remember { mutableStateOf(false) }

    val sortOptions = listOf(
        SortOrder.DEFAULT  to "Default",
        SortOrder.NAME_ASC to "Name A→Z",
        SortOrder.NAME_DESC to "Name Z→A",
        SortOrder.ID_DESC  to "Newest"
    )

    if (showAddSheet) {
        TuningEditorSheet(
            initialTuning = Tuning("", "[E2,A2,D3,G3,B3,E4]"),
            onDismiss = { showAddSheet = false },
            onConfirm = { tuning -> onSaveTuning(tuning); showAddSheet = false }
        )
    }

    bottomSheetTuning?.let { tuning ->
        TuningEditorSheet(
            initialTuning = tuning,
            onDismiss = { bottomSheetTuning = null },
            onConfirm = { updated -> onSaveTuning(updated); bottomSheetTuning = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddSheet = true },
                expanded = isFabExpanded,
                icon = { Icon(Icons.Filled.Add, contentDescription = "Add tuning") },
                text = { Text("Add Tuning") }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it; onSearchQueryChange(it) },
                onSearch = { searchActive = false },
                active = searchActive,
                onActiveChange = { searchActive = it },
                placeholder = { Text("Search tunings…") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
            ) {}

            LazyRow(
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                items(sortOptions) { (order, label) ->
                    FilterChip(
                        selected = sortOrder == order,
                        onClick = { onSortOrderChange(order) },
                        label = { Text(label) }
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 88.dp)
            ) {
                items(items = tunings, key = { it.id }) { tuning ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                val deleted = tuning
                                onDeleteTuning(deleted)
                                scope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Deleted \"${deleted.name}\"",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        onSaveTuning(deleted)
                                    }
                                }
                                true
                            } else false
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color by animateColorAsState(
                                targetValue = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart)
                                    MaterialTheme.colorScheme.errorContainer
                                else Color.Transparent,
                                label = "dismiss_bg"
                            )
                            Box(
                                modifier = Modifier.fillMaxSize().background(color),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(end = 16.dp)
                                )
                            }
                        }
                    ) {
                        ListItem(
                            headlineContent = { Text(tuning.name) },
                            supportingContent = {
                                Text(tuning.notesFormatted(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            },
                            tonalElevation = if (tuning.id == selectedTuningId) 4.dp else 0.dp,
                            modifier = Modifier.fillMaxWidth()
                                .background(
                                    if (tuning.id == selectedTuningId)
                                        MaterialTheme.colorScheme.surfaceContainer
                                    else MaterialTheme.colorScheme.background
                                )
                        )
                    }

                    // Tap to select, long-press to edit — handled by ListItem modifier clickable
                    // (Separate click handling wrapping the SwipeToDismissBox is below)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TuningEditorSheet(
    initialTuning: Tuning,
    onDismiss: () -> Unit,
    onConfirm: (Tuning) -> Unit
) {
    val notes = remember { NotesStructure.notesAsStringArray }
    val isEditing = initialTuning.id > 0
    val guitarTuning = remember(initialTuning) { TuningHandler.getGuitarTuningFromTuning(initialTuning) }

    var name by remember { mutableStateOf(initialTuning.name) }
    var nameError by remember { mutableStateOf(false) }
    val selectedNotes = remember { mutableStateListOf(*Array(6) { i -> guitarTuning.notes[i].name }) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isEditing) "Edit Tuning" else "Add Tuning",
                style = MaterialTheme.typography.headlineSmall
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it; nameError = false },
                label = { Text("Tuning Name") },
                isError = nameError,
                supportingText = if (nameError) {{ Text("Name is required") }} else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            val stringLabels = listOf("String 1 (Low E)", "String 2 (A)", "String 3 (D)",
                "String 4 (G)", "String 5 (B)", "String 6 (High E)")
            selectedNotes.forEachIndexed { i, selected ->
                NoteDropdown(
                    label = stringLabels[i],
                    selectedNote = selected,
                    notes = notes,
                    onNoteSelected = { selectedNotes[i] = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                OutlinedButton(onClick = onDismiss) { Text("Cancel") }
                Button(onClick = {
                    if (name.isBlank()) { nameError = true; return@Button }
                    val noteArray = Array(6) { i ->
                        NotesStructure.searchNote(selectedNotes[i])!!
                    }
                    val result = Tuning(name.trim(), TuningHandler.getNotesStringFromNotesArray(noteArray))
                    if (isEditing) result.id = initialTuning.id
                    onConfirm(result)
                }) {
                    Text(if (isEditing) "Save" else "Add")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NoteDropdown(
    label: String,
    selectedNote: String,
    notes: Array<String>,
    onNoteSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedNote,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            notes.forEach { note ->
                DropdownMenuItem(
                    text = { Text(note) },
                    onClick = { onNoteSelected(note); expanded = false },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}
```

> **Important**: The `ListItem` modifier in the current code doesn't wire up tap-to-select or long-press-to-edit because `SwipeToDismissBox` captures gestures. The fix is to wrap the `ListItem` content with `Modifier.clickable` and a `pointerInput` for long press. Add the following at the top of the `LazyColumn` items block, replacing the `ListItem`:

```kotlin
// Replace the plain ListItem inside SwipeToDismissBox with:
ListItem(
    headlineContent = { Text(tuning.name) },
    supportingContent = {
        Text(tuning.notesFormatted(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    },
    tonalElevation = if (tuning.id == selectedTuningId) 4.dp else 0.dp,
    modifier = Modifier
        .fillMaxWidth()
        .background(
            if (tuning.id == selectedTuningId)
                MaterialTheme.colorScheme.surfaceContainer
            else MaterialTheme.colorScheme.background
        )
        .combinedClickable(
            onClick = { onSelectTuning(tuning.id) },
            onLongClick = { bottomSheetTuning = tuning }
        )
)
```

Add `import androidx.compose.foundation.combinedClickable` to the imports.

- [ ] **Step 2: Build**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. If `SearchBar` overload is ambiguous, use the `@OptIn(ExperimentalMaterial3Api::class)` annotation at the function level.

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/ui/screens/TuningsScreen.kt
git commit -m "feat: add TuningsScreen with SearchBar, FilterChips, SwipeToDismiss, FAB, BottomSheet"
```

---

## Task 10: TunifyNavHost + New MainActivity

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/ui/navigation/TunifyNavHost.kt`
- Modify: `app/src/main/java/dev/thestbar/tunify/core/activities/MainActivity.kt`

- [ ] **Step 1: Create TunifyNavHost.kt**

```kotlin
package dev.thestbar.tunify.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dev.thestbar.tunify.data.viewmodels.ThemeViewModel
import dev.thestbar.tunify.data.viewmodels.TunerViewModel
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.ui.screens.InfoScreen
import dev.thestbar.tunify.ui.screens.SettingsScreen
import dev.thestbar.tunify.ui.screens.TunerScreen
import dev.thestbar.tunify.ui.screens.TuningsScreen

private sealed class Screen(val route: String, val label: String) {
    object Tuner    : Screen("tuner",    "Tuner")
    object Tunings  : Screen("tunings",  "Tunings")
    object Settings : Screen("settings", "Settings")
    object Info     : Screen("info",     "Info")
}

@Composable
fun TunifyNavHost(
    tunerViewModel: TunerViewModel,
    tuningViewModel: TuningViewModel,
    themeViewModel: ThemeViewModel,
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination

    val screens = listOf(Screen.Tuner, Screen.Tunings, Screen.Settings, Screen.Info)

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    val selected = currentRoute?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = when (screen) {
                                    is Screen.Tuner    -> Icons.Filled.GraphicEq
                                    is Screen.Tunings  -> Icons.Filled.List
                                    is Screen.Settings -> Icons.Filled.Settings
                                    is Screen.Info     -> Icons.Filled.Info
                                },
                                contentDescription = screen.label
                            )
                        },
                        label = { Text(screen.label) }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Tuner.route,
        ) {
            composable(Screen.Tuner.route) {
                val state by tunerViewModel.uiState.collectAsStateWithLifecycle()
                TunerScreen(
                    state = state,
                    onToggleTuning = { tunerViewModel.setIsTuning(!state.isTuning) },
                    modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                )
            }
            composable(Screen.Tunings.route) {
                val tunings by tuningViewModel.filteredTunings.collectAsStateWithLifecycle()
                val sortOrder by tuningViewModel.sortOrder.collectAsStateWithLifecycle()
                val selectedTuningId by tunerViewModel.selectedTuningId.collectAsStateWithLifecycle()
                TuningsScreen(
                    tunings = tunings,
                    selectedTuningId = selectedTuningId,
                    sortOrder = sortOrder,
                    onSearchQueryChange = tuningViewModel::setSearchQuery,
                    onSortOrderChange = tuningViewModel::setSortOrder,
                    onSelectTuning = tunerViewModel::selectTuning,
                    onDeleteTuning = tuningViewModel::delete,
                    onSaveTuning = tuningViewModel::insert,
                    modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                )
            }
            composable(Screen.Settings.route) {
                val themePref by themeViewModel.themePreference.collectAsStateWithLifecycle()
                val isTunerLocked by themeViewModel.isTunerLocked.collectAsStateWithLifecycle()
                val isLoadLastMutedState by themeViewModel.isLoadLastMutedState.collectAsStateWithLifecycle()
                SettingsScreen(
                    themePreference = themePref,
                    isTunerLocked = isTunerLocked,
                    isLoadLastMutedState = isLoadLastMutedState,
                    onThemeChange = themeViewModel::setThemePreference,
                    onTunerLockedChange = themeViewModel::setIsTunerLocked,
                    onLoadLastMutedStateChange = themeViewModel::setIsLoadLastMutedState,
                    onResetDatabase = tuningViewModel::resetToDefaults,
                    modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                )
            }
            composable(Screen.Info.route) {
                InfoScreen(
                    modifier = androidx.compose.ui.Modifier.padding(innerPadding)
                )
            }
        }
    }
}
```

> **Note**: `InfoScreen` currently has no `modifier` parameter — add `modifier: Modifier = Modifier` to its signature and apply it to the root element.

- [ ] **Step 2: Update InfoScreen.kt to accept a modifier parameter**

In `InfoScreen.kt`, change the signature to:

```kotlin
@Composable
fun InfoScreen(modifier: Modifier = Modifier) {
    AndroidView(
        // ...
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    )
}
```

- [ ] **Step 3: Replace MainActivity.kt**

Replace the entire file:

```kotlin
package dev.thestbar.tunify.core.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.data.viewmodels.ThemeViewModel
import dev.thestbar.tunify.data.viewmodels.TunerViewModel
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.ui.navigation.TunifyNavHost
import dev.thestbar.tunify.ui.theme.TunifyTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

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
        lifecycleScope.run {
            // Seed DB on first launch — runBlocking is intentional here to complete before setContent
        }
        // Use lifecycle scope to avoid blocking main thread
        kotlinx.coroutines.GlobalScope.run {
            // Use viewModel scope via ViewModel factory instead
        }
        // Actual implementation: delegate to TuningViewModel which already has the repository
        tuningViewModel // ViewModel init handles nothing here; seed via below
        lifecycleScope.let {
            it.launchWhenCreated {
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
}
```

> **Note**: `launchWhenCreated` is deprecated. Replace with `lifecycleScope.launch`:
>
> ```kotlin
> private fun initDatabase() {
>     lifecycleScope.launch {
>         try {
>             val initialized = PreferencesDataStoreHandler
>                 .hasBeenInitialized(applicationContext).first() ?: false
>             if (!initialized) {
>                 TuningHandler.resetDatabaseValuesToDefault(TuningRepository(application))
>                 PreferencesDataStoreHandler.setHasBeenInitialized(applicationContext, true)
>             }
>         } catch (e: NullPointerException) {
>             TuningHandler.resetDatabaseValuesToDefault(TuningRepository(application))
>             PreferencesDataStoreHandler.setHasBeenInitialized(applicationContext, true)
>         }
>     }
> }
> ```

Use this clean version of `initDatabase()` in the final file. The complete clean `MainActivity.kt`:

```kotlin
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
```

- [ ] **Step 4: Build**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. The old fragments and adapter will still exist and compile; the new Compose path is now also wired. This means the app has two entry paths temporarily — that's expected.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/ui/navigation/TunifyNavHost.kt \
        app/src/main/java/dev/thestbar/tunify/core/activities/MainActivity.kt \
        app/src/main/java/dev/thestbar/tunify/ui/screens/InfoScreen.kt
git commit -m "feat: add TunifyNavHost, replace MainActivity with Compose setContent"
```

---

## Task 11: Delete Old Code

At this point the Compose path is fully wired. Remove all Fragment/View-based code that is now dead.

**Files to delete:**
- `app/src/main/java/dev/thestbar/tunify/core/fragments/MainFragment.kt`
- `app/src/main/java/dev/thestbar/tunify/core/fragments/TuningsFragment.kt`
- `app/src/main/java/dev/thestbar/tunify/core/fragments/SettingsFragment.kt`
- `app/src/main/java/dev/thestbar/tunify/core/fragments/InfoFragment.kt`
- `app/src/main/java/dev/thestbar/tunify/core/fragments/AddTuningDialogFragment.kt`
- `app/src/main/java/dev/thestbar/tunify/core/TuningAdapter.kt`
- `app/src/main/res/layout/fragment_main.xml`
- `app/src/main/res/layout/fragment_tunings.xml`
- `app/src/main/res/layout/fragment_settings.xml`
- `app/src/main/res/layout/fragment_info.xml`
- `app/src/main/res/layout/fragment_add_tuning_dialog.xml`
- `app/src/main/res/layout/main_app_screen.xml`
- `app/src/main/res/layout/activity_main.xml`
- `app/src/main/res/layout/tuning_list_item.xml`
- `app/src/main/res/layout/info_app_scene.xml`
- `app/src/main/res/layout/settings_app_scene.xml`
- `app/src/main/res/layout/tunings_app_screen.xml`
- `app/src/main/res/layout/test_audio.xml`
- `app/src/main/res/navigation/nav_graph.xml`
- `app/src/main/res/menu/bottom_navigation_menu.xml`

- [ ] **Step 1: Delete all old Fragment classes and TuningAdapter**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/fragments/MainFragment.kt
rm app/src/main/java/dev/thestbar/tunify/core/fragments/TuningsFragment.kt
rm app/src/main/java/dev/thestbar/tunify/core/fragments/SettingsFragment.kt
rm app/src/main/java/dev/thestbar/tunify/core/fragments/InfoFragment.kt
rm app/src/main/java/dev/thestbar/tunify/core/fragments/AddTuningDialogFragment.kt
rm app/src/main/java/dev/thestbar/tunify/core/TuningAdapter.kt
```

- [ ] **Step 2: Delete all XML layouts, nav graph, and menu**

```bash
rm app/src/main/res/layout/fragment_main.xml
rm app/src/main/res/layout/fragment_tunings.xml
rm app/src/main/res/layout/fragment_settings.xml
rm app/src/main/res/layout/fragment_info.xml
rm app/src/main/res/layout/fragment_add_tuning_dialog.xml
rm app/src/main/res/layout/main_app_screen.xml
rm app/src/main/res/layout/activity_main.xml
rm app/src/main/res/layout/tuning_list_item.xml
rm app/src/main/res/layout/info_app_scene.xml
rm app/src/main/res/layout/settings_app_scene.xml
rm app/src/main/res/layout/tunings_app_screen.xml
rm app/src/main/res/layout/test_audio.xml
rm app/src/main/res/navigation/nav_graph.xml
rm app/src/main/res/menu/bottom_navigation_menu.xml
```

- [ ] **Step 3: Remove now-unused ViewBinding from build.gradle.kts**

In `app/build.gradle.kts`, in the `buildFeatures` block, remove (or keep) `viewBinding = true`. Since all View-based layouts are deleted there are no more `*Binding` classes to generate, but removing the flag is optional. If any code still references a binding class, the build will tell you.

- [ ] **Step 4: Remove Fragment-related dependencies from build.gradle.kts (optional)**

Once fragments are deleted, `androidx.fragment.ktx`, `androidx.navigation.fragment.ktx`, and `androidx.navigation.ui.ktx` are no longer needed. They can be removed from `app/build.gradle.kts` and from `gradle/libs.versions.toml`.

- [ ] **Step 5: Build**

```bash
./gradlew assembleDebug
```

Expected: `BUILD SUCCESSFUL`. Fix any remaining compilation errors — they will be import references to deleted classes.

- [ ] **Step 6: Run tests**

```bash
./gradlew test
```

Expected: all unit tests pass.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "feat: delete all Fragments, XML layouts, and View adapter — Compose migration complete"
```

---

## Self-Review Checklist

After all tasks are complete:

- [ ] Confirm `./gradlew assembleDebug` passes with zero warnings about unresolved references
- [ ] Confirm `./gradlew test` passes
- [ ] Install on a device/emulator: verify dark theme, light theme, system theme all apply correctly
- [ ] Verify tuner tab: toggle button starts/stops audio, note updates, speedometer needle moves
- [ ] Verify tunings tab: search filters list, filter chips change sort, swipe-left deletes + undo works, FAB opens add sheet, long-press opens edit sheet
- [ ] Verify settings tab: theme segmented button persists across restart, lock tuner switch saves, reset DB shows dialog and clears + reseeds
- [ ] Verify info tab: links are tappable
- [ ] Verify that selecting a tuning on the Tunings tab updates the string chips on the Tuner tab
