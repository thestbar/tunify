# Kotlin Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert all 21 Java source files in `app/src/main/java/com/junkiedan/junkietuner/` to idiomatic Kotlin, rename the package to `dev.thestbar.tunify`, adopt ViewBinding, replace `AsyncTask`/RxJava with Coroutines/Flow, and add pure-logic unit tests.

**Architecture:** Bottom-up by layer — Phase 0 sets up the build (KSP, ViewBinding, coroutines, parcelize, package rename), Phase 1 migrates pure logic with full test coverage, Phase 2 migrates the data layer (highest-risk: AsyncTask→Coroutines, RxJava→Flow, ViewModel singleton→standard ViewModel), Phase 3 migrates the UI layer with ViewBinding, Phase 4 cleans up dead RxJava deps and leftover Java files. The branch carries red CI through Phases 2 and most of 3; full app verification happens at end of Phase 3.

**Tech Stack:** Kotlin 2.1.21, Android Gradle Plugin 8.7.0, KSP, kotlinx.coroutines, kotlinx.coroutines-test, AndroidX ViewBinding, kotlin-parcelize, Room 2.8.4 (suspend + Flow), DataStore Preferences 1.2.1 (Flow), AndroidX Lifecycle (viewModelScope, lifecycleScope, repeatOnLifecycle).

**Reference spec:** `docs/superpowers/specs/2026-05-30-kotlin-migration-design.md`

**Branch strategy:** Create a single feature branch `kotlin-migration` at the start. All 25 commits land on this branch. CI may be red after Phase 2 and partially through Phase 3; this is expected. Merge to `main` only after the Phase 3 smoke test passes.

---

## Phase 0 — Build setup & package rename

### Task 0.1: Build configuration + package rename

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`
- Move: `app/src/main/java/com/junkiedan/junkietuner/` → `app/src/main/java/dev/thestbar/tunify/`
- Move: `app/src/test/java/com/junkiedan/junkietuner/` → `app/src/test/java/dev/thestbar/tunify/`
- Move: `app/src/androidTest/java/com/junkiedan/junkietuner/` → `app/src/androidTest/java/dev/thestbar/tunify/`
- Modify: every `.java` file's `package` declaration and `com.junkiedan.junkietuner` imports

- [ ] **Step 1: Create feature branch**

```bash
git checkout -b kotlin-migration
```

- [ ] **Step 2: Add new entries to `gradle/libs.versions.toml`**

Append under `[versions]`:

```toml
ksp = "2.1.21-2.0.2"
coroutines = "1.10.2"
lifecycle = "2.9.1"
fragmentKtx = "1.8.6"
activityKtx = "1.10.1"
```

Append under `[libraries]`:

```toml
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycle" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-fragment-ktx = { group = "androidx.fragment", name = "fragment-ktx", version.ref = "fragmentKtx" }
androidx-activity-ktx = { group = "androidx.activity", name = "activity-ktx", version.ref = "activityKtx" }
```

Append under `[plugins]`:

```toml
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
kotlin-parcelize = { id = "org.jetbrains.kotlin.plugin.parcelize", version.ref = "kotlin" }
```

- [ ] **Step 3: Rewrite `app/build.gradle.kts`**

Replace the entire file with:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.ksp)
}

android {
    namespace = "dev.thestbar.tunify"
    compileSdk = 36

    defaultConfig {
        applicationId = "dev.thestbar.tunify"
        minSdk = 24
        targetSdk = 36
        versionCode = 2
        versionName = "1.1"
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
    }
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.kotlinx.coroutines.android)
    // Room
    implementation(libs.room.runtime)
    ksp(libs.room.compiler)
    implementation(libs.room.rxjava3)
    implementation(libs.room.guava)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)
    // DataStore
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.preferences.rxjava3)
    // RxJava (still required until Phase 4)
    implementation(libs.rxandroid)
    implementation(libs.rxjava3)
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

- [ ] **Step 4: Move source directories**

```bash
mkdir -p app/src/main/java/dev/thestbar
git mv app/src/main/java/com/junkiedan/junkietuner app/src/main/java/dev/thestbar/tunify
rmdir app/src/main/java/com/junkiedan 2>/dev/null

mkdir -p app/src/test/java/dev/thestbar
git mv app/src/test/java/com/junkiedan/junkietuner app/src/test/java/dev/thestbar/tunify
rmdir app/src/test/java/com/junkiedan 2>/dev/null

mkdir -p app/src/androidTest/java/dev/thestbar
git mv app/src/androidTest/java/com/junkiedan/junkietuner app/src/androidTest/java/dev/thestbar/tunify
rmdir app/src/androidTest/java/com/junkiedan 2>/dev/null
```

- [ ] **Step 5: Rewrite package declarations and imports across all .java files**

Run (BSD sed on macOS — note the `-i ''` empty backup argument):

```bash
find app/src/main/java/dev/thestbar/tunify app/src/test/java/dev/thestbar/tunify app/src/androidTest/java/dev/thestbar/tunify \
  -name "*.java" -print0 | xargs -0 sed -i '' \
  -e 's|com\.junkiedan\.junkietuner|dev.thestbar.tunify|g'
```

- [ ] **Step 6: Update `AndroidManifest.xml`**

The activity `android:name=".core.activities.MainActivity"` uses a relative path, so the `namespace` change handles it. No changes needed inside the manifest itself. Verify:

```bash
grep -n "junkietuner\|junkiedan" app/src/main/AndroidManifest.xml
```

Expected: no matches.

- [ ] **Step 7: Verify nothing in the codebase still references the old package**

```bash
git grep -n "com\.junkiedan\|junkietuner" app/src
```

Expected: no matches (besides possibly comments — fix any found).

- [ ] **Step 8: Build the app**

```bash
./gradlew --no-daemon assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 9: Smoke test (manual)**

Launch on an emulator or device. Verify:
1. App launches
2. Each of the 4 bottom-nav tabs opens (Main, Tunings, Settings, Info)
3. No crashes

- [ ] **Step 10: Commit**

```bash
git add -A
git commit -m "$(cat <<'EOF'
chore: set up Kotlin migration build (KSP, ViewBinding, parcelize, coroutines) and rename package to dev.thestbar.tunify

- Add KSP, parcelize plugins; enable ViewBinding
- Add kotlinx-coroutines, lifecycle-ktx, fragment-ktx, activity-ktx deps
- Move source folders com/junkiedan/junkietuner -> dev/thestbar/tunify
- Rewrite package declarations and imports across all .java files
- Update namespace and applicationId

No behavioral changes. All .java files remain Java; this commit is
purely the rename + build wiring needed for the Kotlin migration.
EOF
)"
```

---

## Phase 1 — Pure logic layer

### Task 1.1: Convert `Note` to Kotlin data class + add `NoteTest`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/util/notes/Note.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/util/notes/Note.java`
- Test: `app/src/test/java/dev/thestbar/tunify/util/notes/NoteTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/dev/thestbar/tunify/util/notes/NoteTest.kt`:

```kotlin
package dev.thestbar.tunify.util.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class NoteTest {

    @Test
    fun `notes with same name and frequency are equal`() {
        val a = Note("A4", 440.0)
        val b = Note("A4", 440.0)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `notes with different frequencies are not equal`() {
        assertNotEquals(Note("A4", 440.0), Note("A4", 441.0))
    }

    @Test
    fun `toString includes name and frequency`() {
        val s = Note("A4", 440.0).toString()
        assertEquals(true, s.contains("A4"))
        assertEquals(true, s.contains("440.0"))
    }
}
```

- [ ] **Step 2: Run the test, expect compile failure**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.notes.NoteTest"
```

Expected: compilation fails because `Note.kt` does not exist yet, OR existing Java `Note` lacks `equals` (compiles, tests fail).

- [ ] **Step 3: Create `Note.kt` and delete `Note.java`**

Create `app/src/main/java/dev/thestbar/tunify/util/notes/Note.kt`:

```kotlin
package dev.thestbar.tunify.util.notes

data class Note(val name: String, val frequency: Double)
```

Delete the old Java file:

```bash
rm app/src/main/java/dev/thestbar/tunify/util/notes/Note.java
```

- [ ] **Step 4: Run the test, expect pass**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.notes.NoteTest"
```

Expected: all 3 tests pass.

- [ ] **Step 5: Verify the app still builds**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL. Java callers (e.g., `NotesStructure.java`) keep using `Note.getName()` / `Note.getFrequency()` — Kotlin `data class` exposes these as JVM bean-style getters automatically.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: convert Note to Kotlin data class with NoteTest"
```

---

### Task 1.2: Convert `PitchDetectionAlgorithm` to Kotlin `fun interface`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/util/algorithms/PitchDetectionAlgorithm.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/util/algorithms/PitchDetectionAlgorithm.java`

No test for this one — single-method interface with no logic.

- [ ] **Step 1: Create `PitchDetectionAlgorithm.kt`**

```kotlin
package dev.thestbar.tunify.util.algorithms

fun interface PitchDetectionAlgorithm {
    fun getPitch(inputBuffer: ShortArray): Double
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/util/algorithms/PitchDetectionAlgorithm.java
```

- [ ] **Step 3: Update the Java caller `Yin.java`**

Modify `app/src/main/java/dev/thestbar/tunify/util/algorithms/Yin.java`. The Java signature in `Yin` was `public double getPitch(short[] inputBuffer)`. Java's `short[]` and Kotlin's `ShortArray` are the same JVM type (`short[]`), so no changes are needed in the Java file. Verify:

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: convert PitchDetectionAlgorithm to Kotlin fun interface"
```

---

### Task 1.3: Convert `NotesStructure` to Kotlin `object` + add `NotesStructureTest`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/util/notes/NotesStructure.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/util/notes/NotesStructure.java`
- Test: `app/src/test/java/dev/thestbar/tunify/util/notes/NotesStructureTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/dev/thestbar/tunify/util/notes/NotesStructureTest.kt`:

```kotlin
package dev.thestbar.tunify.util.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class NotesStructureTest {

    @Test
    fun `allNotes contains 96 notes`() {
        assertEquals(96, NotesStructure.allNotes.size)
    }

    @Test
    fun `A4 is the concert pitch 440 Hz`() {
        val a4 = NotesStructure.searchNote("A4")
        assertNotNull(a4)
        assertEquals(440.0, a4!!.frequency, 1e-9)
    }

    @Test
    fun `A0 is at 27_50 Hz`() {
        val a0 = NotesStructure.searchNote("A0")
        assertNotNull(a0)
        assertEquals(27.5, a0!!.frequency, 1e-9)
    }

    @Test
    fun `unknown note name returns null`() {
        assertNull(NotesStructure.searchNote("Z9"))
    }

    @Test
    fun `searchNoteIndex returns -1 for unknown name`() {
        assertEquals(-1, NotesStructure.searchNoteIndex("Z9"))
    }

    @Test
    fun `searchNoteIndex finds A4 at expected position`() {
        val idx = NotesStructure.searchNoteIndex("A4")
        assertEquals(440.0, NotesStructure.allNotes[idx].frequency, 1e-9)
    }

    @Test
    fun `notesAsStringArray length equals allNotes length`() {
        assertEquals(NotesStructure.allNotes.size, NotesStructure.notesAsStringArray.size)
    }

    @Test
    fun `octave 1 below A4 is A3 at 220 Hz`() {
        val a3 = NotesStructure.searchNote("A3")
        assertNotNull(a3)
        assertEquals(220.0, a3!!.frequency, 1e-9)
    }

    @Test
    fun `octave 1 above A4 is A5 at 880 Hz`() {
        val a5 = NotesStructure.searchNote("A5")
        assertNotNull(a5)
        assertEquals(880.0, a5!!.frequency, 1e-9)
    }
}
```

- [ ] **Step 2: Run the test, expect failure**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.notes.NotesStructureTest"
```

Expected: compile error referring to `NotesStructure.allNotes` / `NotesStructure.notesAsStringArray` (the Java version is static-method only).

- [ ] **Step 3: Create `NotesStructure.kt`**

```kotlin
package dev.thestbar.tunify.util.notes

import kotlin.math.pow

/**
 * The full note spectrum the tuner can detect: 96 notes covering A0 (27.5 Hz) to G#8 (~6644.88 Hz),
 * centered on concert pitch A4 = 440 Hz.
 */
object NotesStructure {

    private const val CONCERT_PITCH = 440.0
    private const val NOTES_PER_OCTAVE = 12
    private const val OCTAVES = 8
    private val notesAnno = arrayOf(
        "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"
    )

    val allNotes: Array<Note> by lazy { buildAllNotes() }

    val notesAsStringArray: Array<String> by lazy {
        Array(allNotes.size) { allNotes[it].name }
    }

    fun searchNote(noteName: String): Note? =
        allNotes.firstOrNull { it.name == noteName }

    fun searchNoteIndex(noteName: String): Int =
        allNotes.indexOfFirst { it.name == noteName }

    private fun buildAllNotes(): Array<Note> {
        val total = NOTES_PER_OCTAVE * OCTAVES
        val notes = arrayOfNulls<Note>(total)
        val center = total / 2
        notes[center] = Note("A4", CONCERT_PITCH)
        notes[0] = Note("A0", 27.50)

        var currOctFwd = 4
        var currOctBck = 4
        for (i in 1 until center) {
            val fwd = CONCERT_PITCH * 2.0.pow(i / 12.0)
            val fwdIdx = center + i
            val fwdNoteIdx = (fwdIdx + 9) % 12
            if (fwdNoteIdx == 0) currOctFwd++
            notes[fwdIdx] = Note(notesAnno[fwdNoteIdx] + currOctFwd, fwd)

            val bck = CONCERT_PITCH * 2.0.pow(-i / 12.0)
            val bckIdx = center - i
            val bckNoteIdx = (bckIdx + 9) % 12
            if (bckNoteIdx == 11) currOctBck--
            notes[bckIdx] = Note(notesAnno[bckNoteIdx] + currOctBck, bck)
        }
        @Suppress("UNCHECKED_CAST")
        return notes as Array<Note>
    }
}
```

- [ ] **Step 4: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/util/notes/NotesStructure.java
```

- [ ] **Step 5: Update Java callers**

Several Java files call `NotesStructure.getAllNotes()` and `NotesStructure.getNotesAsStringArray()` (instance-style getters). With the Kotlin `object` exposing `val`s, these become `NotesStructure.INSTANCE.getAllNotes()` from Java — but Kotlin `val` on an `object` is exposed as a static field. To make Java code keep compiling without changes, add `@JvmStatic` semantics. Update `NotesStructure.kt`:

Replace the `val allNotes` and `val notesAsStringArray` declarations with:

```kotlin
    @JvmStatic
    val allNotes: Array<Note> by lazy { buildAllNotes() }

    @JvmStatic
    val notesAsStringArray: Array<String> by lazy {
        Array(allNotes.size) { allNotes[it].name }
    }

    @JvmStatic
    fun searchNote(noteName: String): Note? =
        allNotes.firstOrNull { it.name == noteName }

    @JvmStatic
    fun searchNoteIndex(noteName: String): Int =
        allNotes.indexOfFirst { it.name == noteName }
```

This exposes static accessors `NotesStructure.getAllNotes()`, `NotesStructure.getNotesAsStringArray()`, `NotesStructure.searchNote(...)`, `NotesStructure.searchNoteIndex(...)` to Java callers, matching the Java signatures used today.

- [ ] **Step 6: Run the test, expect pass**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.notes.NotesStructureTest"
```

Expected: all 9 tests pass.

- [ ] **Step 7: Verify the app builds**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "refactor: convert NotesStructure to Kotlin object with @JvmStatic accessors + add NotesStructureTest"
```

---

### Task 1.4: Convert `Yin` to Kotlin instance class + add `YinTest`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/util/algorithms/Yin.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/util/algorithms/Yin.java`
- Test: `app/src/test/java/dev/thestbar/tunify/util/algorithms/YinTest.kt`
- Modify: `app/src/main/java/dev/thestbar/tunify/core/RecordingRunnable.java` (one-line: `Yin.getInstance(recorder.getSampleRate())` → `new Yin((double) recorder.getSampleRate())`)

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/dev/thestbar/tunify/util/algorithms/YinTest.kt`:

```kotlin
package dev.thestbar.tunify.util.algorithms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.sin

class YinTest {

    private val sampleRate = 44100.0

    @Test
    fun `detects 440 Hz sine wave within 1 Hz`() {
        val pitch = detect(440.0)
        assertEquals(440.0, pitch, 1.0)
    }

    @Test
    fun `detects 220 Hz sine wave within 1 Hz`() {
        val pitch = detect(220.0)
        assertEquals(220.0, pitch, 1.0)
    }

    @Test
    fun `detects 880 Hz sine wave within 2 Hz`() {
        val pitch = detect(880.0)
        assertEquals(880.0, pitch, 2.0)
    }

    @Test
    fun `silent input returns -1`() {
        val yin = Yin(sampleRate)
        val buffer = ShortArray(8192)
        val pitch = yin.getPitch(buffer)
        assertEquals(-1.0, pitch, 1e-9)
    }

    @Test
    fun `two instances do not share state`() {
        val a = Yin(44100.0)
        val b = Yin(22050.0)
        val buffer = sineWave(440.0, 8192, 44100.0)
        // a is at 44100 Hz so will detect ~440 Hz; b is at 22050 Hz so will detect ~220 Hz
        val pitchA = a.getPitch(buffer)
        val pitchB = b.getPitch(buffer)
        assertEquals(440.0, pitchA, 1.0)
        assertEquals(220.0, pitchB, 1.0)
    }

    private fun detect(frequency: Double): Double {
        val yin = Yin(sampleRate)
        val buffer = sineWave(frequency, 8192, sampleRate)
        return yin.getPitch(buffer)
    }

    private fun sineWave(frequency: Double, samples: Int, sampleRate: Double): ShortArray {
        val out = ShortArray(samples)
        val amplitude = Short.MAX_VALUE / 2
        for (i in 0 until samples) {
            out[i] = (amplitude * sin(2.0 * PI * frequency * i / sampleRate)).toInt().toShort()
        }
        return out
    }
}
```

- [ ] **Step 2: Run the test, expect failure**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.algorithms.YinTest"
```

Expected: compile error — no `Yin(Double)` constructor exists; only `Yin.getInstance(int)`.

- [ ] **Step 3: Create `Yin.kt`**

```kotlin
package dev.thestbar.tunify.util.algorithms

/**
 * Yin Pitch Detection Algorithm (de Cheveigné & Kawahara, 2002).
 * Each instance holds its own working buffer; no global singleton.
 */
class Yin(private val sampleRate: Double) : PitchDetectionAlgorithm {

    private var inputBuffer: ShortArray = ShortArray(0)
    private var yinBuffer: DoubleArray = DoubleArray(0)

    override fun getPitch(inputBuffer: ShortArray): Double {
        this.inputBuffer = inputBuffer
        if (yinBuffer.size != inputBuffer.size / 2) {
            yinBuffer = DoubleArray(inputBuffer.size / 2)
        }

        difference()
        cumulativeMeanNormalizedDifference()
        val threshold = absoluteThreshold()

        return if (threshold != -1) {
            val optimized = parabolicInterpolation(threshold)
            sampleRate / optimized
        } else {
            -1.0
        }
    }

    private fun difference() {
        val len = yinBuffer.size
        for (tau in 0 until len) yinBuffer[tau] = 0.0
        for (tau in 1 until len) {
            for (j in 0 until len) {
                val diff = inputBuffer[j].toDouble() - inputBuffer[j + tau].toDouble()
                yinBuffer[tau] += diff * diff
            }
        }
    }

    private fun cumulativeMeanNormalizedDifference() {
        val len = yinBuffer.size
        yinBuffer[0] = 1.0
        var currSum = yinBuffer[1]
        yinBuffer[1] = 1.0
        for (tau in 2 until len) {
            currSum += yinBuffer[tau]
            yinBuffer[tau] *= tau / currSum
        }
    }

    private fun absoluteThreshold(): Int {
        val len = yinBuffer.size
        var tau = 2
        while (tau < len) {
            if (yinBuffer[tau] < GLOBAL_MINIMUM_THRESHOLD) {
                while (tau + 1 < len && yinBuffer[tau + 1] < yinBuffer[tau]) {
                    tau++
                }
                return tau
            }
            tau++
        }
        return -1
    }

    private fun parabolicInterpolation(threshold: Int): Double {
        val x0 = if (threshold < 1) threshold else threshold - 1
        val x2 = if (threshold + 1 < yinBuffer.size) threshold + 1 else threshold
        if (x0 == threshold) {
            return if (yinBuffer[threshold] <= yinBuffer[x2]) threshold.toDouble() else x2.toDouble()
        }
        if (x2 == threshold) {
            return if (yinBuffer[threshold] <= yinBuffer[x0]) threshold.toDouble() else x0.toDouble()
        }
        val s0 = yinBuffer[x0]
        val s1 = yinBuffer[threshold]
        val s2 = yinBuffer[x2]
        return threshold + 0.5 * (s2 - s0) / (2.0 * s1 - s2 - s0)
    }

    companion object {
        private const val GLOBAL_MINIMUM_THRESHOLD = 0.15
    }
}
```

- [ ] **Step 4: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/util/algorithms/Yin.java
```

- [ ] **Step 5: Update `RecordingRunnable.java` caller**

The Java line `yinInstance = Yin.getInstance(recorder.getSampleRate());` no longer compiles. Change it to use the new constructor.

In `app/src/main/java/dev/thestbar/tunify/core/RecordingRunnable.java`, find:

```java
yinInstance = Yin.getInstance(recorder.getSampleRate());
```

Replace with:

```java
yinInstance = new Yin((double) recorder.getSampleRate());
```

- [ ] **Step 6: Run the test, expect pass**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.algorithms.YinTest"
```

Expected: all 5 tests pass.

- [ ] **Step 7: Verify the app builds**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 8: Commit**

```bash
git add -A
git commit -m "refactor: convert Yin to Kotlin instance class with YinTest

The Java version was a singleton that silently ignored the sampleRate
argument after first construction. The Kotlin version takes sampleRate
as a constructor parameter so two callers with different sample rates
can coexist. Updates RecordingRunnable to construct a new Yin instead
of calling the removed Yin.getInstance(int)."
```

---

### Task 1.5: Convert `NoteDetection` to Kotlin class with companion + add `NoteDetectionTest`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/util/algorithms/NoteDetection.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/util/algorithms/NoteDetection.java`
- Test: `app/src/test/java/dev/thestbar/tunify/util/algorithms/NoteDetectionTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/dev/thestbar/tunify/util/algorithms/NoteDetectionTest.kt`:

```kotlin
package dev.thestbar.tunify.util.algorithms

import dev.thestbar.tunify.util.notes.Note
import dev.thestbar.tunify.util.notes.NotesStructure
import org.junit.Assert.assertEquals
import org.junit.Test

class NoteDetectionTest {

    private val nd = NoteDetection(NotesStructure.allNotes)

    @Test
    fun `frequency at or below A0 returns A0`() {
        val a0 = NotesStructure.searchNote("A0")!!
        assertEquals(a0, nd.findClosestNote(10.0))
        assertEquals(a0, nd.findClosestNote(a0.frequency))
    }

    @Test
    fun `frequency at or above last note returns last note`() {
        val last = NotesStructure.allNotes.last()
        assertEquals(last, nd.findClosestNote(20000.0))
        assertEquals(last, nd.findClosestNote(last.frequency))
    }

    @Test
    fun `frequency exactly at A4 returns A4`() {
        val a4 = NotesStructure.searchNote("A4")!!
        assertEquals(a4, nd.findClosestNote(440.0))
    }

    @Test
    fun `frequency just above A4 is still closest to A4`() {
        val a4 = NotesStructure.searchNote("A4")!!
        assertEquals(a4, nd.findClosestNote(441.0))
    }

    @Test
    fun `frequency halfway between A4 and A#4 picks the closer side`() {
        val a4 = NotesStructure.searchNote("A4")!!
        val aSharp4 = NotesStructure.searchNote("A#4")!!
        // A#4 is just over 466 Hz; halfway is ~453 Hz; just below halfway should be A4
        val belowMid = (a4.frequency + aSharp4.frequency) / 2 - 0.5
        assertEquals(a4, nd.findClosestNote(belowMid))
        // Just above halfway should be A#4
        val aboveMid = (a4.frequency + aSharp4.frequency) / 2 + 0.5
        assertEquals(aSharp4, nd.findClosestNote(aboveMid))
    }

    @Test
    fun `cents between same frequency is zero`() {
        val a4 = NotesStructure.searchNote("A4")!!
        assertEquals(0.0, NoteDetection.getDifferentInCents(a4, 440.0), 1e-6)
    }

    @Test
    fun `cents from A4 to A5 is 1200`() {
        val a4 = NotesStructure.searchNote("A4")!!
        assertEquals(1200.0, NoteDetection.getDifferentInCents(a4, 880.0), 1e-6)
    }

    @Test
    fun `cents from A4 to A#4 is 100`() {
        val a4 = NotesStructure.searchNote("A4")!!
        val aSharp4 = NotesStructure.searchNote("A#4")!!
        assertEquals(100.0, NoteDetection.getDifferentInCents(a4, aSharp4.frequency), 1e-6)
    }

    @Test
    fun `cents with zero-frequency note returns -1`() {
        val zero = Note("ZERO", 0.0)
        assertEquals(-1.0, NoteDetection.getDifferentInCents(zero, 100.0), 1e-9)
    }

    @Test
    fun `compareClosestNoteToTarget picks closer note`() {
        val a = Note("A", 100.0)
        val b = Note("B", 200.0)
        assertEquals(a, NoteDetection.compareClosestNoteToTarget(a, b, 110.0))
        assertEquals(b, NoteDetection.compareClosestNoteToTarget(a, b, 190.0))
    }
}
```

- [ ] **Step 2: Run the test, expect failure**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.algorithms.NoteDetectionTest"
```

Expected: tests may compile but `findClosestNote` for `441.0` may return A4 (Java does), and `compareClosestNoteToTarget`/`getDifferentInCents` need verification. If any fail, that's OK — Step 3 makes them pass.

- [ ] **Step 3: Create `NoteDetection.kt`**

```kotlin
package dev.thestbar.tunify.util.algorithms

import dev.thestbar.tunify.util.notes.Note
import kotlin.math.abs
import kotlin.math.log10

class NoteDetection(private val allNotes: Array<Note>) {

    fun findClosestNote(frequency: Double): Note {
        val len = allNotes.size
        if (frequency <= allNotes[0].frequency) return allNotes[0]
        if (frequency >= allNotes[len - 1].frequency) return allNotes[len - 1]

        var low = 0
        var high = len
        var mid = 0
        while (low < high) {
            mid = (low + high) / 2
            if (allNotes[mid].frequency == frequency) return allNotes[mid]
            if (frequency < allNotes[mid].frequency) {
                if (mid > 0 && frequency > allNotes[mid - 1].frequency) {
                    return compareClosestNoteToTarget(allNotes[mid - 1], allNotes[mid], frequency)
                }
                high = mid
            } else {
                if (mid < len - 1 && frequency < allNotes[mid + 1].frequency) {
                    return compareClosestNoteToTarget(allNotes[mid], allNotes[mid + 1], frequency)
                }
                low = mid + 1
            }
        }
        return allNotes[mid]
    }

    companion object {
        private val LOG2_TO_LOG10_CONVERSION_CONST = 1200.0 * (1 / log10(2.0))

        @JvmStatic
        fun compareClosestNoteToTarget(note1: Note, note2: Note, frequency: Double): Note {
            val delta1 = abs(note1.frequency - frequency)
            val delta2 = abs(note2.frequency - frequency)
            return if (delta1 < delta2) note1 else note2
        }

        @JvmStatic
        fun getDifferentInCents(note: Note?, frequency: Double): Double {
            if (note == null || note.frequency == 0.0) return -1.0
            val delta = frequency / note.frequency
            return LOG2_TO_LOG10_CONVERSION_CONST * log10(delta)
        }
    }
}
```

- [ ] **Step 4: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/util/algorithms/NoteDetection.java
```

- [ ] **Step 5: Run the test, expect pass**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.algorithms.NoteDetectionTest"
```

Expected: all 10 tests pass.

- [ ] **Step 6: Verify the app builds**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL. Java callers (`RecordingRunnable`, `MainFragment`) keep using `NoteDetection.getDifferentInCents(note, hz)` thanks to `@JvmStatic`.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: convert NoteDetection to Kotlin class with companion object + add NoteDetectionTest"
```

---

### Task 1.6: Convert `GuitarTuning` to `@Parcelize` Kotlin class + add `GuitarTuningTest`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/util/notes/GuitarTuning.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/util/notes/GuitarTuning.java`
- Test: `app/src/test/java/dev/thestbar/tunify/util/notes/GuitarTuningTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/dev/thestbar/tunify/util/notes/GuitarTuningTest.kt`:

```kotlin
package dev.thestbar.tunify.util.notes

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class GuitarTuningTest {

    @Test
    fun `valid 6-note construction`() {
        val tuning = GuitarTuning("Standard E", arrayOf("E2", "A2", "D3", "G3", "B3", "E4"))
        assertEquals("Standard E", tuning.tuningName)
        assertEquals(6, tuning.notes.size)
        assertEquals("E2", tuning.notes[0].name)
        assertEquals("E4", tuning.notes[5].name)
    }

    @Test
    fun `unknown note name falls back to A0`() {
        val tuning = GuitarTuning("X", arrayOf("E2", "A2", "Z9", "G3", "B3", "E4"))
        assertEquals("A0", tuning.notes[2].name)
    }

    @Test
    fun `array size not equal to 6 throws`() {
        assertThrows(IllegalArgumentException::class.java) {
            GuitarTuning("X", arrayOf("E2", "A2", "D3"))
        }
        assertThrows(IllegalArgumentException::class.java) {
            GuitarTuning("X", arrayOf("E2", "A2", "D3", "G3", "B3", "E4", "F5"))
        }
    }
}
```

- [ ] **Step 2: Run the test, expect failure**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.notes.GuitarTuningTest"
```

Expected: compile or assertion failure — the Java version uses `assert` (only fires when assertions enabled) instead of always-on `require`.

- [ ] **Step 3: Create `GuitarTuning.kt`**

```kotlin
package dev.thestbar.tunify.util.notes

import android.os.Parcelable
import android.util.Log
import kotlinx.parcelize.Parcelize

@Parcelize
class GuitarTuning(
    val tuningName: String,
    noteNames: Array<String>
) : Parcelable {

    val notes: Array<Note>

    init {
        require(noteNames.size == 6) {
            "GuitarTuning requires exactly 6 notes, got ${noteNames.size}"
        }
        notes = Array(6) { i ->
            NotesStructure.searchNote(noteNames[i]) ?: run {
                Log.e(
                    "GuitarTuning",
                    "Search for invalid note - noteName: `${noteNames[i]}` - using A0 instead"
                )
                NotesStructure.searchNote("A0")!!
            }
        }
    }
}
```

- [ ] **Step 4: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/util/notes/GuitarTuning.java
```

- [ ] **Step 5: Run the test, expect pass**

```bash
./gradlew test --tests "dev.thestbar.tunify.util.notes.GuitarTuningTest"
```

Expected: all 3 tests pass.

- [ ] **Step 6: Verify the app builds**

```bash
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL. Java callers (`MainFragment`, `AddTuningDialogFragment`, `TuningHandler`) keep using `tuning.getNotes()` and `tuning.getTuningName()` via Kotlin's auto-generated property getters.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: convert GuitarTuning to @Parcelize Kotlin class + add GuitarTuningTest

Replaces Serializable with Parcelable via kotlin-parcelize.
Replaces Java assert (only fires when -ea enabled) with require()
so the size invariant always holds."
```

---

## Phase 2 — Data layer

> **Important:** At the end of Phase 2 the app module will not compile — the still-Java UI layer references API surfaces that we're rewriting (static `TuningViewModel` methods, `Flowable`-returning DataStore methods, `LiveData`-returning DAO). The build only returns to green when Phase 3 rewires the UI. After each Phase 2 task, run `./gradlew compileDebugKotlin` (Kotlin code only) instead of `assembleDebug` — that compiles the new Kotlin files without compiling the Java UI.

### Task 2.1: Convert `Tuning` entity to Kotlin data class + add `TuningTest`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/data/entities/Tuning.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/data/entities/Tuning.java`
- Test: `app/src/test/java/dev/thestbar/tunify/data/entities/TuningTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/dev/thestbar/tunify/data/entities/TuningTest.kt`:

```kotlin
package dev.thestbar.tunify.data.entities

import org.junit.Assert.assertEquals
import org.junit.Test

class TuningTest {

    @Test
    fun `notesFormatted converts bracketed comma string to double-space separated`() {
        val tuning = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]")
        assertEquals("E2  A2  D3  G3  B3  E4", tuning.notesFormatted())
    }

    @Test
    fun `notesFormatted handles sharps`() {
        val tuning = Tuning("Standard Eb", "[D#2,G#2,C#3,F#3,A#3,D#4]")
        assertEquals("D#2  G#2  C#3  F#3  A#3  D#4", tuning.notesFormatted())
    }

    @Test
    fun `notesFormatted handles single-note tuning`() {
        val tuning = Tuning("One", "[A4]")
        assertEquals("A4", tuning.notesFormatted())
    }

    @Test
    fun `notesFormatted handles empty brackets`() {
        val tuning = Tuning("Empty", "[]")
        assertEquals("", tuning.notesFormatted())
    }

    @Test
    fun `equality is based on all fields including id`() {
        val a = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]").apply { id = 1 }
        val b = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]").apply { id = 1 }
        val c = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]").apply { id = 2 }
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assert(a != c)
    }
}
```

- [ ] **Step 2: Run the test, expect failure**

```bash
./gradlew test --tests "dev.thestbar.tunify.data.entities.TuningTest"
```

Expected: equality test fails (Java `Tuning` doesn't override equals/hashCode).

- [ ] **Step 3: Create `Tuning.kt`**

```kotlin
package dev.thestbar.tunify.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tuning(
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "notes") var notes: String
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    fun notesFormatted(): String {
        val sb = StringBuilder()
        val len = notes.length
        for (i in 1 until len - 1) {
            if (notes[i] == ',') sb.append("  ") else sb.append(notes[i])
        }
        return sb.toString()
    }
}
```

- [ ] **Step 4: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/data/entities/Tuning.java
```

- [ ] **Step 5: Run the test, expect pass**

```bash
./gradlew test --tests "dev.thestbar.tunify.data.entities.TuningTest"
```

Expected: all 5 tests pass.

- [ ] **Step 6: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL. Java callers still reference `tuning.name`, `tuning.notes`, `tuning.id` directly — Kotlin `var` fields are exposed as `public` JVM fields when `@JvmField`-annotated... but here we're using property accessors. Since Java callers use direct field access (`tuning.name = ...`), we need `@JvmField`. Add `@JvmField` to each `var`:

Update `Tuning.kt`:

```kotlin
package dev.thestbar.tunify.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Tuning(
    @JvmField @ColumnInfo(name = "name") var name: String,
    @JvmField @ColumnInfo(name = "notes") var notes: String
) {
    @JvmField
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

    fun notesFormatted(): String {
        val sb = StringBuilder()
        val len = notes.length
        for (i in 1 until len - 1) {
            if (notes[i] == ',') sb.append("  ") else sb.append(notes[i])
        }
        return sb.toString()
    }
}
```

Re-run:

```bash
./gradlew compileDebugKotlin
./gradlew test --tests "dev.thestbar.tunify.data.entities.TuningTest"
```

Expected: both pass.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: convert Tuning entity to Kotlin data class + add TuningTest

Uses @JvmField on the public fields so existing Java callers
(TuningHandler, RecordingRunnable etc.) continue to access them
as fields instead of getter/setter pairs."
```

---

### Task 2.2: Convert `TuningDao` to Kotlin interface with `suspend` + `Flow`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/data/dao/TuningDao.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/data/dao/TuningDao.java`

No test (DAO is just declarations; the test scope per the spec is pure logic only).

- [ ] **Step 1: Create `TuningDao.kt`**

```kotlin
package dev.thestbar.tunify.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import dev.thestbar.tunify.data.entities.Tuning
import kotlinx.coroutines.flow.Flow

@Dao
interface TuningDao {

    @Query("SELECT * FROM Tuning")
    fun getAllTunings(): Flow<List<Tuning>>

    @Query("SELECT * FROM Tuning WHERE id = :id")
    fun getTuningById(id: Int): Flow<Tuning?>

    @Update
    suspend fun update(tuning: Tuning)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg tunings: Tuning)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOne(tuning: Tuning)

    @Delete
    suspend fun delete(tuning: Tuning)

    @Query("DELETE FROM Tuning")
    suspend fun deleteAll()
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/data/dao/TuningDao.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL (Java UI compilation will fail later; this step only compiles Kotlin sources).

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: convert TuningDao to Kotlin interface with suspend + Flow

Write methods become suspend functions. Read methods return Flow
instead of LiveData. App module will not fully compile until
the data layer and UI are rewired (Phases 2-3)."
```

---

### Task 2.3: Convert `JunkieTunerAppDatabase` to `TunifyDatabase` (Kotlin)

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/data/databases/TunifyDatabase.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/data/databases/JunkieTunerAppDatabase.java`

- [ ] **Step 1: Create `TunifyDatabase.kt`**

```kotlin
package dev.thestbar.tunify.data.databases

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.thestbar.tunify.data.dao.TuningDao
import dev.thestbar.tunify.data.entities.Tuning

@Database(entities = [Tuning::class], version = 1)
abstract class TunifyDatabase : RoomDatabase() {

    abstract fun tuningDao(): TuningDao

    companion object {
        @Volatile
        private var INSTANCE: TunifyDatabase? = null

        fun getDatabase(context: Context): TunifyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TunifyDatabase::class.java,
                    "app_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = false)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
```

Note: the on-disk file name stays `"app_db"` so existing user data persists. The class rename has no effect on Room's schema (which is keyed off the entity, not the database class).

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/data/databases/JunkieTunerAppDatabase.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: rename JunkieTunerAppDatabase to TunifyDatabase (Kotlin)

Database file name 'app_db' unchanged - existing user data persists.
Uses standard Kotlin double-checked-locking singleton in companion object."
```

---

### Task 2.4: Convert `TuningRepository` to Kotlin (Coroutines, no AsyncTask)

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/data/TuningRepository.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/data/TuningRepository.java`

- [ ] **Step 1: Create `TuningRepository.kt`**

```kotlin
package dev.thestbar.tunify.data

import android.app.Application
import dev.thestbar.tunify.data.dao.TuningDao
import dev.thestbar.tunify.data.databases.TunifyDatabase
import dev.thestbar.tunify.data.entities.Tuning
import kotlinx.coroutines.flow.Flow

class TuningRepository(application: Application) {

    private val tuningDao: TuningDao =
        TunifyDatabase.getDatabase(application).tuningDao()

    fun getAllTunings(): Flow<List<Tuning>> = tuningDao.getAllTunings()

    fun getTuningById(id: Int): Flow<Tuning?> = tuningDao.getTuningById(id)

    suspend fun insert(tuning: Tuning) = tuningDao.insertOne(tuning)

    suspend fun update(tuning: Tuning) = tuningDao.update(tuning)

    suspend fun delete(tuning: Tuning) = tuningDao.delete(tuning)

    suspend fun deleteAll() = tuningDao.deleteAll()
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/data/TuningRepository.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: convert TuningRepository to Kotlin with suspend + Flow

Removes the four AsyncTask inner classes. Writes are now suspending
delegations to the DAO. Reads return Flow."
```

---

### Task 2.5: Convert `TuningViewModel` to proper `AndroidViewModel`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/data/viewmodels/TuningViewModel.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/data/viewmodels/TuningViewModel.java`

- [ ] **Step 1: Create `TuningViewModel.kt`**

```kotlin
package dev.thestbar.tunify.data.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.data.entities.Tuning
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TuningViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TuningRepository(application)

    val allTunings: StateFlow<List<Tuning>> = repository.getAllTunings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = emptyList()
        )

    fun getTuningById(id: Int): Flow<Tuning?> = repository.getTuningById(id)

    fun insert(tuning: Tuning) {
        viewModelScope.launch { repository.insert(tuning) }
    }

    fun update(tuning: Tuning) {
        viewModelScope.launch { repository.update(tuning) }
    }

    fun delete(tuning: Tuning) {
        viewModelScope.launch { repository.delete(tuning) }
    }

    fun deleteAll() {
        viewModelScope.launch { repository.deleteAll() }
    }
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/data/viewmodels/TuningViewModel.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: rewrite TuningViewModel as standard AndroidViewModel

Drops the manual static-method singleton. Exposes allTunings as a
StateFlow with WhileSubscribed(5s) sharing. Write methods launch in
viewModelScope. Consumers obtain it via by viewModels() or
by activityViewModels() — UI rewire happens in Phase 3."
```

---

### Task 2.6: Convert `TuningHandler` to Kotlin `object` + add `TuningHandlerTest`

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/data/TuningHandler.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/data/TuningHandler.java`
- Test: `app/src/test/java/dev/thestbar/tunify/data/TuningHandlerTest.kt`

- [ ] **Step 1: Write the failing test**

Create `app/src/test/java/dev/thestbar/tunify/data/TuningHandlerTest.kt`:

```kotlin
package dev.thestbar.tunify.data

import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.util.notes.NotesStructure
import org.junit.Assert.assertEquals
import org.junit.Test

class TuningHandlerTest {

    @Test
    fun `getGuitarTuningFromTuning parses standard E correctly`() {
        val tuning = Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]")
        val gt = TuningHandler.getGuitarTuningFromTuning(tuning)
        assertEquals("Standard E", gt.tuningName)
        assertEquals(6, gt.notes.size)
        assertEquals("E2", gt.notes[0].name)
        assertEquals("E4", gt.notes[5].name)
    }

    @Test
    fun `getNotesStringFromNotesArray produces canonical bracketed string`() {
        val notes = arrayOf(
            NotesStructure.searchNote("E2")!!,
            NotesStructure.searchNote("A2")!!,
            NotesStructure.searchNote("D3")!!,
            NotesStructure.searchNote("G3")!!,
            NotesStructure.searchNote("B3")!!,
            NotesStructure.searchNote("E4")!!
        )
        assertEquals("[E2,A2,D3,G3,B3,E4]", TuningHandler.getNotesStringFromNotesArray(notes))
    }

    @Test
    fun `round-trip Tuning - GuitarTuning - String yields original notes string`() {
        val original = Tuning("X", "[E2,A2,D3,G3,B3,E4]")
        val gt = TuningHandler.getGuitarTuningFromTuning(original)
        val roundTripped = TuningHandler.getNotesStringFromNotesArray(gt.notes)
        assertEquals(original.notes, roundTripped)
    }
}
```

- [ ] **Step 2: Run the test, expect failure**

```bash
./gradlew test --tests "dev.thestbar.tunify.data.TuningHandlerTest"
```

Expected: compile error — `getGuitarTuningFromTuning` and `getNotesStringFromNotesArray` exist on Java but referenced through the wrong package or test may fail on a subtle aspect.

- [ ] **Step 3: Create `TuningHandler.kt`**

The Java version has a giant `resetDatabaseValuesToDefault(Application)` method that calls static `TuningViewModel.insert(application, tuning)` ~115 times. With the new ViewModel that's no longer possible. Replace it with a `suspend fun` that takes the `TuningRepository`.

```kotlin
package dev.thestbar.tunify.data

import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.util.notes.GuitarTuning
import dev.thestbar.tunify.util.notes.Note

object TuningHandler {

    @JvmStatic
    fun getGuitarTuningFromTuning(tuning: Tuning): GuitarTuning {
        val notesStr = tuning.notes.substring(1, tuning.notes.length - 1)
        val notes = notesStr.split(",").toTypedArray()
        require(notes.isNotEmpty()) { "Trying to create guitar tuning with 0 notes." }
        return GuitarTuning(tuning.name, notes)
    }

    @JvmStatic
    fun getNotesStringFromNotesArray(notes: Array<Note>): String =
        notes.joinToString(separator = ",", prefix = "[", postfix = "]") { it.name }

    /**
     * Wipes the database and re-inserts the bundled defaults.
     * Suspending — invoke from a coroutine scope (e.g., viewModelScope).
     */
    suspend fun resetDatabaseValuesToDefault(repository: TuningRepository) {
        repository.deleteAll()
        DEFAULT_TUNINGS.forEach { (name, notes) ->
            repository.insert(Tuning(name, notes))
        }
    }

    private val DEFAULT_TUNINGS: List<Pair<String, String>> = listOf(
        // Standard tunings
        "Standard E" to "[E2,A2,D3,G3,B3,E4]",
        "Standard Eb/D#" to "[D#2,G#2,C#3,F#3,A#3,D#4]",
        "Standard D" to "[D2,G2,C3,F3,A3,D4]",
        "Standard Db/C#" to "[C#2,F#2,B3,E3,G#3,C#4]",
        "Standard C" to "[C2,F2,A#3,D#3,G3,C4]",
        "Standard F" to "[F2,A#2,D#3,G#3,C3,F4]",
        "Standard G" to "[G2,C3,F3,A#3,D3,G4]",
        // Drop tunings
        "Drop D ('DDD')" to "[D2,A2,D3,G3,B3,E4]",
        "Double Drop D" to "[D2,A2,D3,G3,B3,D4]",
        "Drop C ('Neon')" to "[C2,A2,D3,G3,B3,E4]",
        "Low Drop C" to "[C2,G2,C3,F3,A3,D4]",
        "Drop A ('Slack Thwack')" to "[A1,A2,D3,G3,B3,E4]",
        // Open tunings
        "Open D ('Vestapol')" to "[D2,A2,D3,F#3,A3,D4]",
        "Open Dm ('Bentonia')" to "[D2,A2,D3,F3,A3,D4]",
        "Open Dsus ('DADGAD')" to "[D2,A2,D3,G3,A3,D4]",
        "Open G ('Taro Patch')" to "[D2,G2,D3,G3,B3,D4]",
        "Open Gm ('Banjo Minor')" to "[D2,G2,D3,G3,A#3,D4]",
        "Open Gsus ('Sawmill')" to "[D2,G2,D3,G3,C4,D4]",
        "Open C ('Wide Major')" to "[C2,G2,C3,G3,C4,E4]",
        "Open Cm ('Wide Minor')" to "[C2,G2,C3,G3,C4,D#4]",
        "Open Csus ('Wide Modal')" to "[C2,G2,C3,G3,C4,F4]",
        "Open E ('Vestapol')" to "[E2,B2,E3,G#3,B4,E4]",
        "Open Em ('Cross note')" to "[E2,B2,E3,G3,B4,E4]",
        "Open F ('Low Taro')" to "[C2,F2,C3,F3,A3,C4]",
        "Open Fm ('Low Banjo')" to "[C2,F2,C3,F3,A3,C4]",
        "Open A ('Spanish')" to "[E2,A2,E3,A3,C#4,E4]",
        // Interval tunings
        "All Minor 3rds ('Diminished')" to "[G2,A#2,C#3,E3,G4,A#4]",
        "All Minor 3rds ('Augmented')" to "[E2,G#2,C3,E3,G#3,C4]",
        "All Perfect 4ths ('Regular')" to "[E2,A2,D3,G3,C4,F4]",
        "All Tritones ('Symmetric')" to "[C2,F#2,C3,F#3,C4,F#4]",
        "All Perfect 5ths ('Quintal')" to "[A2,E3,B3,F#4,C#5,G#5]",
        "All Minor 6ths ('Aug. Flip')" to "[F2,C#3,A3,F4,C#5,A5]",
        // Global tunings
        "Ali Farka Touré" to "[G2,A2,D3,G3,B3,E4]",
        "Atta's C" to "[C2,G2,E3,G3,C4,E4]",
        "Bağlama/Saz" to "[G2,G3,D3,D4,A3,A4]",
        "Carnatic ('Drake's Drone')" to "[B2,E3,B3,E4,B4,E5]",
        "Charango" to "[G2,G3,C3,E3,A3,E4]",
        "Haja's Bb" to "[A#2,F3,C3,F3,A#3,C4]",
        "Jack's Chikari" to "[D2,D2,D3,G3,B3,E4]",
        "Kabosy" to "[C2,G2,D3,G3,B3,D4]",
        "Mauna Loa C6" to "[C2,G2,C3,G3,A3,E4]",
        "Mi-composé ('Elenga')" to "[E2,A2,D4,G3,B3,E4]",
        "Orkney" to "[C2,G2,D3,G3,C4,D4]",
        "Oud (Arabic)" to "[E2,A2,C#3,F#3,B3,E4]",
        "Oud (Turkish)" to "[E2,A2,B2,E3,A3,D4]",
        "Papuan Four-Key" to "[F2,A#2,C3,F3,A3,C4]",
        "Rakotomavo" to "[A#2,F3,C3,G3,C4,E4]",
        "Keola's C ('Wahine')" to "[C2,G2,D3,G3,B3,E4]",
        "Zen Drone ('Dulcimeric')" to "[D2,A2,D3,A3,A3,D4]",
        // Artist / track tunings
        "AirTap" to "[F2,A2,C3,F3,C4,F4]",
        "Albert Collins Fm" to "[F2,C3,F3,G#3,C4,F4]",
        "Albert King F6" to "[C2,F2,C3,F3,A3,D4]",
        "Black Crow" to "[A#2,A#3,C#4,F4,A4,A#4]",
        "Blown a Wish" to "[F2,C3,F3,A#3,A#3,G4]",
        "Bruce Palmer ('Judy Blue Eyes')" to "[E2,E2,E3,E3,B3,E4]",
        "Cello ('Haircut')" to "[C2,G2,D3,A3,B3,E4]",
        "Coyote" to "[C2,G2,D3,F3,C4,E4]",
        "Dracula" to "[C2,G2,C3,F3,A#3,D4]",
        "Equilibrium" to "[G1,A2,D3,E3,A3,E4]",
        "Ethereal" to "[D2,A2,C#3,F#3,C#4,D4]",
        "Fripp's New Standard ('Crafty')" to "[C2,G2,D3,A3,E4,G4]",
        "Funky Avocado" to "[B1,A2,D3,G3,A3,D4]",
        "Gambale" to "[A2,D3,G3,C4,E4,A4]",
        "Ghost Reveries" to "[D2,A2,D3,F3,A3,E4]",
        "Godzilla" to "[C#2,G#2,D#3,E3,B3,E4]",
        "Gothic" to "[D#2,G2,D3,A3,A#3,C4]",
        "Hejira" to "[C2,G2,D3,F3,G3,C4]",
        "I Only Said" to "[E2,A2,B2,G3,G3,E4]",
        "Iris" to "[B1,D2,D3,D3,D4,D4]",
        "José González & Jigsaw Falling Into Place" to "[D2,A2,D3,F#3,B3,E4]",
        "Karnivool" to "[B1,F#2,B2,G3,B3,E4]",
        "Magic Farmer" to "[C2,F2,C3,G3,A3,E4]",
        "Road" to "[E2,A2,D3,E3,B3,E4]",
        "Only Shallow" to "[E2,B2,E3,F#3,B3,E4]",
        "One-Tone Drone ('Ostrich')" to "[D2,D3,D3,D3,D4,D4]",
        "Pink Moon" to "[C2,G2,C3,F3,C4,E4]",
        "Place to Be" to "[C2,G2,C3,F3,G3,E4]",
        "Schizophrenia" to "[F#2,F#2,G3,G3,A3,A3]",
        "Teardrop" to "[D2,A2,D3,A3,B3,E4]",
        "Wind of Change" to "[D2,D2,D3,A3,D4,F#4]",
        "Yvette's Dadd9" to "[D2,A2,D3,F#3,A3,E4]",
        // Miscellaneous tunings
        "Alphabet" to "[A2,B3,C4,D4,E4,F4]",
        "Banjo/Overtones" to "[G2,G2,D3,G3,B3,D4]",
        "Cabbage" to "[C2,A2,A#2,A3,G4,E5]",
        "Drop DG" to "[D2,G2,D3,G3,B3,E4]",
        "Fuji" to "[A#1,G2,D3,G3,G3,D4]",
        "Icarus" to "[D2,A2,D3,G3,B3,C4]",
        "Mesopotamian" to "[B1,A2,G3,D4,A4,D5]",
        "Lefty Flip ('Mirrored')" to "[E2,B2,G2,D3,A3,E4]",
        "Lute/Vihuela" to "[E2,A2,D3,F#3,B3,E4]",
        "Math Rock F" to "[F2,A2,C3,G3,C4,E4]",
        "Nashville" to "[E3,A3,D4,G4,B3,E4]",
        "Overtone Series" to "[G2,B2,D3,F3,G3,A3]",
        "Papa-Papa" to "[D2,A2,D3,D3,A3,D4]",
        "Ten Years" to "[D2,E2,C3,A3,D4,E4]",
        "Ead-Gad" to "[E2,A2,D3,G3,A3,D4]",
        "Zigzag 3rds (Minor)" to "[F2,G#2,C3,D#3,G3,A#3]",
        "Zigzag 3rds (Major)" to "[F2,A2,C3,E3,G3,B3]"
    )
}
```

- [ ] **Step 4: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/data/TuningHandler.java
```

- [ ] **Step 5: Run the test, expect pass**

```bash
./gradlew test --tests "dev.thestbar.tunify.data.TuningHandlerTest"
```

Expected: all 3 tests pass.

- [ ] **Step 6: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "refactor: convert TuningHandler to Kotlin object + add TuningHandlerTest

resetDatabaseValuesToDefault is now a suspend fun taking a
TuningRepository (was Application + static TuningViewModel.insert).
Default tunings extracted to a private List<Pair<String, String>>
for cleanliness."
```

---

### Task 2.7: Convert `PreferencesDataStoreHandler` to Kotlin (suspend + Flow)

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/core/PreferencesDataStoreHandler.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/core/PreferencesDataStoreHandler.java`

No test (file is a thin DataStore wrapper; per spec test scope is pure logic only).

- [ ] **Step 1: Create `PreferencesDataStoreHandler.kt`**

```kotlin
package dev.thestbar.tunify.core

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

object PreferencesDataStoreHandler {

    private val CURRENT_TUNING_ID = intPreferencesKey("current_tuning_id")
    private val HAS_BEEN_INITIALIZED = booleanPreferencesKey("has_been_initialized")
    private val IS_TUNER_LOCKED = booleanPreferencesKey("is_tuner_locked")
    private val IS_LOAD_LAST_MUTED_STATE = booleanPreferencesKey("is_load_last_muted_state")
    private val IS_TUNING = booleanPreferencesKey("is_tuning")

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
}
```

Note: the on-disk file name `"settings"` is preserved — existing user preferences persist.

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/PreferencesDataStoreHandler.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL. The full app build will fail — Java UI files still call the removed Flowable/static-write APIs. That's expected.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: convert PreferencesDataStoreHandler to Kotlin (Flow + suspend)

Reads return Flow<Boolean?>/Flow<Int?>. Writes are suspending fns.
Uses the standard preferencesDataStore extension property. DataStore
file name 'settings' preserved - user preferences persist.

App build will be red until Phase 3 rewires the UI callers."
```

---

## Phase 3 — UI layer

> **Important:** This phase rewires every UI file to the new Coroutines/Flow APIs and adopts ViewBinding throughout. The full app build returns to green only after the last task (3.8). Verify each file compiles with `./gradlew compileDebugKotlin` after each task. Run `./gradlew assembleDebug` at the end of task 3.8.

### Task 3.1: Convert `RecordingRunnable` to Kotlin

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/core/RecordingRunnable.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/core/RecordingRunnable.java`

- [ ] **Step 1: Create `RecordingRunnable.kt`**

```kotlin
package dev.thestbar.tunify.core

import android.app.Activity
import android.media.AudioRecord
import android.widget.TextView
import com.github.anastr.speedviewlib.SpeedView
import dev.thestbar.tunify.core.fragments.MainFragment.Companion.NEEDLE_ANIMATION_SPEED
import dev.thestbar.tunify.util.algorithms.NoteDetection
import dev.thestbar.tunify.util.algorithms.Yin
import dev.thestbar.tunify.util.notes.NotesStructure
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToLong

class RecordingRunnable(
    private val mainActivity: Activity,
    private val recordingInProgress: AtomicBoolean,
    private val recorder: AudioRecord,
    private val pitchTextView: TextView,
    private val speedView: SpeedView,
    private val inputBuffer: ShortArray
) : Thread() {

    private val yinInstance = Yin(recorder.sampleRate.toDouble())

    init {
        if (noteDetection == null) {
            noteDetection = NoteDetection(NotesStructure.allNotes)
        }
    }

    override fun run() {
        val len = inputBuffer.size
        while (recordingInProgress.get()) {
            recorder.read(inputBuffer, 0, len)
            val pitchInHz = yinInstance.getPitch(inputBuffer)
            if (!pitchInHz.isFinite() || pitchInHz == -1.0) continue

            val nd = noteDetection ?: continue
            val closestNote = nd.findClosestNote(pitchInHz)
            val deltaInCents = NoteDetection.getDifferentInCents(closestNote, pitchInHz)

            mainActivity.runOnUiThread {
                pitchTextView.text = closestNote.name
                speedView.speedTo(deltaInCents.roundToLong().toFloat(), NEEDLE_ANIMATION_SPEED)
            }
        }
    }

    companion object {
        @Volatile
        private var noteDetection: NoteDetection? = null

        @JvmStatic
        @Synchronized
        fun setNoteDetection(newNoteDetection: NoteDetection) {
            noteDetection = newNoteDetection
        }
    }
}
```

This depends on `MainFragment.Companion.NEEDLE_ANIMATION_SPEED` — which doesn't exist yet (still in Java). Temporarily, use the literal until `MainFragment` migrates. Replace the import with the literal:

Remove `import dev.thestbar.tunify.core.fragments.MainFragment.Companion.NEEDLE_ANIMATION_SPEED` and replace `NEEDLE_ANIMATION_SPEED` in the body with `300L`. Add a `// TODO: reference MainFragment.NEEDLE_ANIMATION_SPEED once MainFragment is Kotlin` comment is **forbidden** by repo conventions — instead, leave a local `private const val NEEDLE_ANIMATION_SPEED = 300L` at the top of the file, to be removed in Task 3.7.

Update the file:

```kotlin
package dev.thestbar.tunify.core

import android.app.Activity
import android.media.AudioRecord
import android.widget.TextView
import com.github.anastr.speedviewlib.SpeedView
import dev.thestbar.tunify.util.algorithms.NoteDetection
import dev.thestbar.tunify.util.algorithms.Yin
import dev.thestbar.tunify.util.notes.NotesStructure
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.roundToLong

private const val NEEDLE_ANIMATION_SPEED = 300L

class RecordingRunnable(
    private val mainActivity: Activity,
    private val recordingInProgress: AtomicBoolean,
    private val recorder: AudioRecord,
    private val pitchTextView: TextView,
    private val speedView: SpeedView,
    private val inputBuffer: ShortArray
) : Thread() {

    private val yinInstance = Yin(recorder.sampleRate.toDouble())

    init {
        if (noteDetection == null) {
            noteDetection = NoteDetection(NotesStructure.allNotes)
        }
    }

    override fun run() {
        val len = inputBuffer.size
        while (recordingInProgress.get()) {
            recorder.read(inputBuffer, 0, len)
            val pitchInHz = yinInstance.getPitch(inputBuffer)
            if (!pitchInHz.isFinite() || pitchInHz == -1.0) continue

            val nd = noteDetection ?: continue
            val closestNote = nd.findClosestNote(pitchInHz)
            val deltaInCents = NoteDetection.getDifferentInCents(closestNote, pitchInHz)

            mainActivity.runOnUiThread {
                pitchTextView.text = closestNote.name
                speedView.speedTo(deltaInCents.roundToLong().toFloat(), NEEDLE_ANIMATION_SPEED)
            }
        }
    }

    companion object {
        @Volatile
        private var noteDetection: NoteDetection? = null

        @JvmStatic
        @Synchronized
        fun setNoteDetection(newNoteDetection: NoteDetection) {
            noteDetection = newNoteDetection
        }
    }
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/RecordingRunnable.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: convert RecordingRunnable to Kotlin

NEEDLE_ANIMATION_SPEED is duplicated locally; removed in Task 3.7
once MainFragment.NEEDLE_ANIMATION_SPEED becomes a Kotlin const."
```

---

### Task 3.2: Convert `TuningAdapter` to Kotlin with ViewBinding

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/core/TuningAdapter.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/core/TuningAdapter.java`

- [ ] **Step 1: Find the ViewBinding class name for `tuning_list_item.xml`**

ViewBinding generates a class based on the layout name: `tuning_list_item.xml` → `TuningListItemBinding`.

- [ ] **Step 2: Create `TuningAdapter.kt`**

```kotlin
package dev.thestbar.tunify.core

import android.app.Application
import android.content.Context
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import dev.thestbar.tunify.R
import dev.thestbar.tunify.core.fragments.AddTuningDialogFragment
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.TuningListItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class TuningAdapter(
    private var tuningList: List<Tuning>,
    private val fragmentManager: FragmentManager,
    private var selectedItemId: Int,
    private val viewModel: TuningViewModel,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<TuningAdapter.ViewHolder>() {

    private lateinit var context: Context
    private var vibrator: Vibrator? = null

    fun setTuningList(tuningList: List<Tuning>) {
        this.tuningList = tuningList
        notifyDataSetChanged()
    }

    inner class ViewHolder(val binding: TuningListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun select() {
            binding.tuningListItemLinearLayoutBackgroundId
                .setBackgroundResource(R.color.custom_taupe_gray)
        }

        fun unselect() {
            binding.tuningListItemLinearLayoutBackgroundId
                .setBackgroundResource(R.color.custom_raisin_black)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        val binding = TuningListItemBinding.inflate(
            LayoutInflater.from(context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tuning = tuningList[holder.layoutPosition]
        viewHolderMap[tuning.id] = holder
        if (selectedItemId == tuning.id) holder.select() else holder.unselect()

        holder.binding.tuningName.text = tuning.name
        holder.binding.tuningNotes.text = tuning.notesFormatted()
        holder.binding.deleteButton.setOnClickListener {
            Log.d("Deleting Tuning From DB", tuning.toString())
            viewModel.delete(tuning)
        }
        holder.binding.root.setOnLongClickListener {
            vibrator?.vibrate(60)
            AddTuningDialogFragment(tuning).show(fragmentManager, "EditTuningDialogFragment")
            true
        }
        holder.binding.root.setOnClickListener {
            vibrator?.vibrate(60)
            Log.d(
                "TuningAdapter.onClick",
                "Value: $tuning has been selected as default Tuning"
            )
            viewHolderMap[selectedItemId]?.unselect()
            selectedItemId = tuning.id
            notifyItemChanged(holder.layoutPosition)
            holder.select()
            coroutineScope.launch {
                PreferencesDataStoreHandler.setCurrentTuningId(context, tuning.id)
            }
        }
    }

    override fun getItemCount(): Int = tuningList.size

    companion object {
        private val viewHolderMap: MutableMap<Int, ViewHolder> = HashMap()
    }
}
```

Note three behavioral upgrades:
- `viewModel: TuningViewModel` is injected (was `TuningViewModel.deleteOne(application, tuning)` static).
- `coroutineScope: CoroutineScope` is injected so the DataStore write can be launched (was `PreferencesDataStoreHandler.setCurrentTuningId(...)` which was fire-and-forget RxJava).
- `notifyDataSetChanged()` is now called in `setTuningList` — Java forgot this; the only reason the UI updated was the redundant `setAdapter` call in `TuningsFragment`. Will be cleaned up in Task 3.6.

- [ ] **Step 3: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/TuningAdapter.java
```

- [ ] **Step 4: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: convert TuningAdapter to Kotlin with ViewBinding

Adopts TuningListItemBinding. Injects TuningViewModel and a
CoroutineScope so writes to the database and DataStore happen
through the new suspend APIs."
```

---

### Task 3.3: Convert `AddTuningDialogFragment` to Kotlin with ViewBinding

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/core/fragments/AddTuningDialogFragment.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/core/fragments/AddTuningDialogFragment.java`

- [ ] **Step 1: Create `AddTuningDialogFragment.kt`**

ViewBinding class for `fragment_add_tuning_dialog.xml` is `FragmentAddTuningDialogBinding`.

```kotlin
package dev.thestbar.tunify.core.fragments

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.FragmentAddTuningDialogBinding
import dev.thestbar.tunify.util.notes.Note
import dev.thestbar.tunify.util.notes.NotesStructure

class AddTuningDialogFragment(
    private val tuning: Tuning = Tuning("", "[E2,A2,D3,G3,B3,E4]")
) : DialogFragment() {

    private var _binding: FragmentAddTuningDialogBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TuningViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTuningDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val spinners = listOf(
            binding.spinnerNote1, binding.spinnerNote2, binding.spinnerNote3,
            binding.spinnerNote4, binding.spinnerNote5, binding.spinnerNote6
        )

        val arrayAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            NotesStructure.notesAsStringArray
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.addTuningCancelButton.setOnClickListener { dismiss() }

        val guitarTuning = TuningHandler.getGuitarTuningFromTuning(tuning)
        binding.newTuningNameInput.setText(tuning.name)

        spinners.forEachIndexed { i, spinner ->
            spinner.adapter = arrayAdapter
            val idx = NotesStructure.searchNoteIndex(guitarTuning.notes[i].name)
            spinner.setSelection(idx)
        }

        binding.addTuningOkButton.setOnClickListener {
            tuning.name = binding.newTuningNameInput.text?.toString()?.trim().orEmpty()
            if (tuning.name.isEmpty()) {
                binding.newTuningNameInput.error = "Tuning Name is Required!"
                binding.newTuningNameInput.hint = "Enter Tuning Name"
                return@setOnClickListener
            }
            val notes: Array<Note> = Array(spinners.size) { i ->
                NotesStructure.searchNote(spinners[i].selectedItem.toString())!!
            }
            tuning.notes = TuningHandler.getNotesStringFromNotesArray(notes)
            viewModel.insert(tuning)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDismiss(dialog: DialogInterface) {
        // intentionally empty - keeps the window non-cancellable from outside taps
    }
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/fragments/AddTuningDialogFragment.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: convert AddTuningDialogFragment to Kotlin with ViewBinding

Uses activityViewModels() for shared TuningViewModel access.
Replaces findViewById with FragmentAddTuningDialogBinding."
```

---

### Task 3.4: Convert `InfoFragment` to Kotlin with ViewBinding

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/core/fragments/InfoFragment.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/core/fragments/InfoFragment.java`

- [ ] **Step 1: Create `InfoFragment.kt`**

```kotlin
package dev.thestbar.tunify.core.fragments

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import dev.thestbar.tunify.databinding.FragmentInfoBinding

class InfoFragment : Fragment() {

    private var _binding: FragmentInfoBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInfoBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.infoMaterialTextViewId.movementMethod = LinkMovementMethod.getInstance()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): InfoFragment = InfoFragment()
    }
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/fragments/InfoFragment.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: convert InfoFragment to Kotlin with ViewBinding"
```

---

### Task 3.5: Convert `SettingsFragment` to Kotlin with ViewBinding + Flow

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/core/fragments/SettingsFragment.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/core/fragments/SettingsFragment.java`

- [ ] **Step 1: Create `SettingsFragment.kt`**

```kotlin
package dev.thestbar.tunify.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TuningViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val ctx = view.context

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    PreferencesDataStoreHandler.getIsTunerLocked(ctx).collect { value ->
                        binding.lockTunerSwitch.isChecked = value == true
                    }
                }
                launch {
                    PreferencesDataStoreHandler.getIsLoadLastMutedState(ctx).collect { value ->
                        binding.loadLastMutedStateSwitch.isChecked = value == true
                    }
                }
            }
        }

        binding.lockTunerSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                PreferencesDataStoreHandler.setIsTunerLocked(ctx, isChecked)
            }
        }
        binding.loadLastMutedStateSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewLifecycleOwner.lifecycleScope.launch {
                PreferencesDataStoreHandler.setIsLoadLastMutedState(ctx, isChecked)
            }
        }

        binding.resetDatabaseTextView.isClickable = true
        binding.resetDatabaseTextView.setOnClickListener { v ->
            MaterialAlertDialogBuilder(v.context)
                .setTitle("Reset Tuning Database")
                .setMessage("All the changes that you made will be lost. The database will " +
                        "contain only the initial tunings. Do you still want to proceed?")
                .setPositiveButton("No") { dialog, _ -> dialog.dismiss() }
                .setNegativeButton("Yes") { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        TuningHandler.resetDatabaseValuesToDefault(
                            TuningRepository(requireActivity().application)
                        )
                    }
                }
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): SettingsFragment = SettingsFragment()
    }
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/fragments/SettingsFragment.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: convert SettingsFragment to Kotlin with ViewBinding + Flow

Replaces RxJava subscribeOn/observeOn/subscribe chain with
lifecycleScope.launch { repeatOnLifecycle(STARTED) { collect } }.
Reset-database now runs in a coroutine."
```

---

### Task 3.6: Convert `TuningsFragment` to Kotlin with ViewBinding + Flow

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/core/fragments/TuningsFragment.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/core/fragments/TuningsFragment.java`

- [ ] **Step 1: Create `TuningsFragment.kt`**

```kotlin
package dev.thestbar.tunify.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.core.TuningAdapter
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.FragmentTuningsBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class TuningsFragment : Fragment() {

    private var _binding: FragmentTuningsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TuningViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTuningsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val initSelectedItemId = PreferencesDataStoreHandler
                .getCurrentTuningId(requireContext())
                .first() ?: -1

            val adapter = TuningAdapter(
                tuningList = emptyList(),
                fragmentManager = childFragmentManager,
                selectedItemId = initSelectedItemId,
                viewModel = viewModel,
                coroutineScope = viewLifecycleOwner.lifecycleScope
            )
            binding.tuningsList.adapter = adapter
            binding.tuningsList.layoutManager = LinearLayoutManager(requireContext())

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.allTunings.collect { tunings ->
                    adapter.setTuningList(tunings)
                }
            }
        }

        binding.addTuningButton.setOnClickListener {
            AddTuningDialogFragment().show(childFragmentManager, "AddTuningDialogFragment")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance(): TuningsFragment = TuningsFragment()
    }
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/fragments/TuningsFragment.java
```

- [ ] **Step 3: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Commit**

```bash
git add -A
git commit -m "refactor: convert TuningsFragment to Kotlin with ViewBinding + Flow

Replaces LiveData.observe with Flow.collect inside repeatOnLifecycle.
Replaces blockingFirst() with suspending .first() on the DataStore Flow."
```

---

### Task 3.7: Convert `MainFragment` to Kotlin with ViewBinding

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/core/fragments/MainFragment.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/core/fragments/MainFragment.java`
- Modify: `app/src/main/java/dev/thestbar/tunify/core/RecordingRunnable.kt` (remove the local `NEEDLE_ANIMATION_SPEED` const and import it from `MainFragment.Companion`)

- [ ] **Step 1: Create `MainFragment.kt`**

```kotlin
package dev.thestbar.tunify.core.fragments

import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.github.anastr.speedviewlib.components.Section
import com.github.anastr.speedviewlib.components.Style
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dev.thestbar.tunify.R
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.core.RecordingRunnable
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.entities.Tuning
import dev.thestbar.tunify.data.viewmodels.TuningViewModel
import dev.thestbar.tunify.databinding.FragmentMainBinding
import dev.thestbar.tunify.util.algorithms.NoteDetection
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TuningViewModel by activityViewModels()

    private val recordingInProgress = AtomicBoolean(false)
    private var recorder: AudioRecord? = null
    private var recordingThread: Thread? = null
    private var buffer: ShortArray? = null
    private var permissionToRecordAccepted: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        permissionToRecordAccepted = ContextCompat.checkSelfPermission(
            requireContext(), android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPitchTextView()
        initSpeedView()
        initSelectedTuning()
        initTuningSwitch()
    }

    override fun onStart() {
        super.onStart()
        stopRecording()
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = requireContext()
            val isLoadLast = PreferencesDataStoreHandler.getIsLoadLastMutedState(ctx).firstOrNull() ?: false
            val isTuning = PreferencesDataStoreHandler.getIsTuning(ctx).firstOrNull() ?: false
            if (isLoadLast && isTuning) startRecording()
        }
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }

    override fun onStop() {
        super.onStop()
        recorder?.release()
        recorder = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initTuningSwitch() {
        if (!permissionToRecordAccepted) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Tuner has no access to microphone")
                .setMessage("In order to use this tuner you need to grant access to the " +
                        " application for the microphone of the device. Go to settings, " +
                        "grant access manually to the device's microphone and restart the" +
                        " application.")
                .setNegativeButton("Close the application") { _, _ -> requireActivity().finish() }
                .setCancelable(false)
                .show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = requireContext()
            val isLoadLast = PreferencesDataStoreHandler.getIsLoadLastMutedState(ctx).firstOrNull()
            val isTuning = PreferencesDataStoreHandler.getIsTuning(ctx).firstOrNull()
            if (isLoadLast == null || isTuning == null) {
                setSwitchChecked(false)
                PreferencesDataStoreHandler.setIsLoadLastMutedState(ctx, true)
                PreferencesDataStoreHandler.setIsTuning(ctx, false)
            } else {
                setSwitchChecked(isLoadLast && isTuning)
            }
        }

        binding.tuningSwitch.setOnClickListener {
            val checked = binding.tuningSwitch.isChecked
            viewLifecycleOwner.lifecycleScope.launch {
                PreferencesDataStoreHandler.setIsTuning(requireContext(), checked)
            }
            if (checked) startRecording() else stopRecording()
        }
    }

    private fun initPitchTextView() {
        binding.textViewPitch.text = ""
    }

    private fun initSpeedView() {
        binding.speedView.apply {
            setMinSpeed(-50f)
            setMaxSpeed(50f)
            clearSections()
            val colorId = ContextCompat.getColor(requireContext(), R.color.custom_vanilla)
            val main = Section(0f, 1f, colorId)
            main.setStyle(Style.ROUND)
            addSections(main)
            speedometerWidth = 8f
            marksNumber = 9
            markStyle = Style.ROUND
            marksPadding = 5f
            markHeight = 10f
            tickNumber = 11
            tickPadding = 20
        }
    }

    private fun initSelectedTuning() {
        val notesTextViewList = listOf(
            binding.textViewNote1, binding.textViewNote2, binding.textViewNote3,
            binding.textViewNote4, binding.textViewNote5, binding.textViewNote6
        )

        viewLifecycleOwner.lifecycleScope.launch {
            val ctx = requireContext()
            val currentId = PreferencesDataStoreHandler.getCurrentTuningId(ctx).firstOrNull() ?: -1

            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getTuningById(currentId).collect { fetched ->
                    val tuning = fetched ?: Tuning("Standard E", "[E2,A2,D3,G3,B3,E4]")
                    val guitarTuning = TuningHandler.getGuitarTuningFromTuning(tuning)
                    notesTextViewList.forEachIndexed { i, tv ->
                        tv.text = guitarTuning.notes[i].name
                    }
                    val isTunerLocked =
                        PreferencesDataStoreHandler.getIsTunerLocked(ctx).firstOrNull() ?: false
                    if (isTunerLocked) {
                        RecordingRunnable.setNoteDetection(NoteDetection(guitarTuning.notes))
                    }
                }
            }
        }
    }

    private fun startRecording() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
            return
        }
        val buf = ShortArray(BUFFER_SIZE).also { buffer = it }
        val rec = AudioRecord(
            MediaRecorder.AudioSource.UNPROCESSED,
            SAMPLING_RATE_IN_HZ,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            BUFFER_SIZE
        )
        recorder = rec
        rec.startRecording()
        recordingInProgress.set(true)
        recordingThread = RecordingRunnable(
            requireActivity(),
            recordingInProgress,
            rec,
            binding.textViewPitch,
            binding.speedView,
            buf
        ).also { it.name = "Recording Thread"; it.start() }
        binding.tuningSwitch.text = SWITCH_TURNED_ON_STR
    }

    private fun stopRecording() {
        val rec = recorder ?: return
        recordingInProgress.set(false)
        rec.stop()
        rec.release()
        recorder = null
        recordingThread = null
        _binding?.let {
            it.textViewPitch.text = ""
            it.tuningSwitch.text = SWITCH_TURNED_OFF_STR
            it.speedView.speedTo(0f, NEEDLE_ANIMATION_SPEED)
        }
    }

    private fun setSwitchChecked(value: Boolean) {
        binding.tuningSwitch.isChecked = value
        viewLifecycleOwner.lifecycleScope.launch {
            PreferencesDataStoreHandler.setIsTuning(requireContext(), value)
        }
    }

    companion object {
        const val NEEDLE_ANIMATION_SPEED = 300L

        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
        private const val SAMPLING_RATE_IN_HZ = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE_FACTOR = 4
        private val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLING_RATE_IN_HZ, CHANNEL_CONFIG, AUDIO_FORMAT
        ) * BUFFER_SIZE_FACTOR

        private const val SWITCH_TURNED_ON_STR = "Tuning"
        private const val SWITCH_TURNED_OFF_STR = "Muted"

        @JvmStatic
        fun newInstance(): MainFragment = MainFragment()
    }
}
```

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/fragments/MainFragment.java
```

- [ ] **Step 3: Remove the temporary `NEEDLE_ANIMATION_SPEED` in `RecordingRunnable.kt`**

In `app/src/main/java/dev/thestbar/tunify/core/RecordingRunnable.kt`:

Find:

```kotlin
private const val NEEDLE_ANIMATION_SPEED = 300L
```

Delete that line. Then add the import:

```kotlin
import dev.thestbar.tunify.core.fragments.MainFragment.Companion.NEEDLE_ANIMATION_SPEED
```

- [ ] **Step 4: Verify Kotlin compiles**

```bash
./gradlew compileDebugKotlin
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: convert MainFragment to Kotlin with ViewBinding + Flow

Replaces all blockingFirst() calls with suspending .firstOrNull()
inside lifecycleScope.launch. Replaces LiveData.observe on the
selected-tuning lookup with Flow.collect in repeatOnLifecycle.
Removes the temporary NEEDLE_ANIMATION_SPEED duplicate in
RecordingRunnable; now imports it from MainFragment.Companion."
```

---

### Task 3.8: Convert `MainActivity` to Kotlin with ViewBinding + Flow

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/core/activities/MainActivity.kt`
- Delete: `app/src/main/java/dev/thestbar/tunify/core/activities/MainActivity.java`

- [ ] **Step 1: Create `MainActivity.kt`**

```kotlin
package dev.thestbar.tunify.core.activities

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dev.thestbar.tunify.R
import dev.thestbar.tunify.core.PreferencesDataStoreHandler
import dev.thestbar.tunify.core.fragments.InfoFragment
import dev.thestbar.tunify.core.fragments.MainFragment
import dev.thestbar.tunify.core.fragments.SettingsFragment
import dev.thestbar.tunify.core.fragments.TuningsFragment
import dev.thestbar.tunify.data.TuningHandler
import dev.thestbar.tunify.data.TuningRepository
import dev.thestbar.tunify.databinding.MainAppScreenBinding
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainAppScreenBinding
    private var permissionToRecordAccepted: Boolean = false

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

        initBottomNavBar()
        initDatabase()
        initSettings()
        askForPermissions()
    }

    private fun initBottomNavBar() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val current = supportFragmentManager.fragments.lastOrNull()?.javaClass
            when (item.itemId) {
                R.id.page_1 -> transitionFragment(MainFragment::class.java, current, "Main Screen")
                R.id.page_2 -> transitionFragment(TuningsFragment::class.java, current, "Tunings Screen")
                R.id.page_3 -> transitionFragment(SettingsFragment::class.java, current, "Settings Screen")
                R.id.page_4 -> transitionFragment(InfoFragment::class.java, current, "Info Screen")
                else -> false
            }
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, systemBars.bottom)
            insets
        }
    }

    private fun initDatabase() {
        lifecycleScope.launch {
            val initialized = PreferencesDataStoreHandler
                .hasBeenInitialized(applicationContext)
                .firstOrNull()
            if (initialized != true) {
                TuningHandler.resetDatabaseValuesToDefault(TuningRepository(application))
                PreferencesDataStoreHandler.setHasBeenInitialized(applicationContext, true)
            }
        }
    }

    private fun initSettings() {
        lifecycleScope.launch {
            val ctx = applicationContext
            if (PreferencesDataStoreHandler.getIsTunerLocked(ctx).firstOrNull() == null) {
                PreferencesDataStoreHandler.setIsTunerLocked(ctx, false)
            }
            if (PreferencesDataStoreHandler.getIsLoadLastMutedState(ctx).firstOrNull() == null) {
                PreferencesDataStoreHandler.setIsLoadLastMutedState(ctx, true)
            }
            if (PreferencesDataStoreHandler.getIsTuning(ctx).firstOrNull() == null) {
                PreferencesDataStoreHandler.setIsTuning(ctx, true)
            }
        }
    }

    private fun askForPermissions() {
        if (ActivityCompat.checkSelfPermission(
                this, android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO_PERMISSION
            )
        } else {
            permissionToRecordAccepted = true
        }
    }

    private fun transitionFragment(
        targetClass: Class<out Fragment>,
        currentClass: Class<out Fragment>?,
        backStackName: String
    ): Boolean {
        if (currentClass == targetClass) return false
        supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, targetClass, null, null)
            .addToBackStack(backStackName)
            .commit()
        return true
    }

    companion object {
        private const val REQUEST_RECORD_AUDIO_PERMISSION = 200
    }
}
```

> **Note on `transitionFragment` signature:** the original Java `transitionFragment` took a `FragmentManager` and the four arguments (`fragmentManager, targetFragmentClass, currentFragmentClass, backStackName`). The Kotlin version uses the class field `supportFragmentManager` directly, simplifying the API. The container view ID `R.id.flFragment` matches what `MainActivity.java` originally used in its (unshown) helper method body.

> **Critical:** Before deleting `MainActivity.java`, open it (`Read` tool on `app/src/main/java/dev/thestbar/tunify/core/activities/MainActivity.java`) and confirm:
> 1. The container view ID used by the original `transitionFragment` (e.g., `R.id.flFragment` vs. `R.id.fragment_container`). Update the Kotlin call accordingly.
> 2. Any logic inside `askForPermissions()` (the body was not shown above) — port it faithfully.

- [ ] **Step 2: Delete the Java file**

```bash
rm app/src/main/java/dev/thestbar/tunify/core/activities/MainActivity.java
```

- [ ] **Step 3: Build the full app**

```bash
./gradlew assembleDebug
```

Expected: **BUILD SUCCESSFUL** — this is the first time the full app compiles since the start of Phase 2.

- [ ] **Step 4: Run all unit tests**

```bash
./gradlew test
```

Expected: all Phase 1 + Phase 2 tests pass (Note, NotesStructure, Yin, NoteDetection, GuitarTuning, Tuning, TuningHandler).

- [ ] **Step 5: Smoke test (manual)**

Install on emulator/device and verify the full app:

1. App launches; the **Main** fragment opens.
2. If RECORD_AUDIO permission was not previously granted, the system prompt appears — grant it.
3. Play a guitar note (or hum / use a tone generator on another device at 440 Hz):
   - The needle moves on the SpeedView.
   - The closest-note text view updates (e.g., "A4").
4. Open the **Tunings** tab — the list of ~115 tunings loads.
5. Tap the floating "+" button → add-tuning dialog opens. Enter a name, pick 6 notes, OK → the new tuning appears in the list.
6. Long-press an existing tuning row → edit dialog opens populated with that tuning's values.
7. Tap a tuning row → it becomes selected (background changes).
8. Tap the delete button on a row → row disappears.
9. Open the **Settings** tab — toggle each switch; force-quit and reopen the app — switch states persist.
10. Tap "Reset Tunings Database" → confirm "Yes" → the list returns to defaults.
11. Open the **Info** tab — the privacy policy link is clickable.
12. Rotate the device — fragment state and tuning list survive.

- [ ] **Step 6: Commit**

```bash
git add -A
git commit -m "refactor: convert MainActivity to Kotlin with ViewBinding + Flow

Final Phase 3 commit. Replaces all three blockingFirst() calls in
initDatabase()/initSettings() with lifecycleScope.launch +
firstOrNull() on the DataStore Flow. The app now builds end-to-end
in Kotlin (with a handful of .java files still present alongside —
deleted in Phase 4)."
```

---

## Phase 4 — Cleanup

### Task 4.1: Remove dead RxJava dependencies

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`

- [ ] **Step 1: Verify each dep is truly unused**

```bash
git grep -l "io.reactivex" app/src
git grep -l "Flowable\|Observable\|Single\|Completable\|Maybe" app/src
git grep -l "RxDataStore\|RxPreferenceDataStoreBuilder" app/src
git grep -l "androidx.room.rxjava3\|androidx.room.guava\|androidx.room.paging" app/src
```

Expected: no matches. If any match exists, stop and update that file first.

- [ ] **Step 2: Remove entries from `gradle/libs.versions.toml`**

Delete these `[versions]` entries:

```toml
rxjava3 = "3.1.9"
rxandroid = "3.0.2"
```

Delete these `[libraries]` entries:

```toml
room-rxjava3 = { group = "androidx.room", name = "room-rxjava3", version.ref = "room" }
room-guava = { group = "androidx.room", name = "room-guava", version.ref = "room" }
room-paging = { group = "androidx.room", name = "room-paging", version.ref = "room" }
datastore-preferences-rxjava3 = { group = "androidx.datastore", name = "datastore-preferences-rxjava3", version.ref = "datastorePreferences" }
rxjava3 = { group = "io.reactivex.rxjava3", name = "rxjava", version.ref = "rxjava3" }
rxandroid = { group = "io.reactivex.rxjava3", name = "rxandroid", version.ref = "rxandroid" }
```

- [ ] **Step 3: Remove entries from `app/build.gradle.kts`**

Delete from the `dependencies` block:

```kotlin
implementation(libs.room.rxjava3)
implementation(libs.room.guava)
implementation(libs.room.paging)
implementation(libs.datastore.preferences.rxjava3)
implementation(libs.rxandroid)
implementation(libs.rxjava3)
```

Also delete the `// RxJava (still required until Phase 4)` comment.

- [ ] **Step 4: Build to verify nothing depended on them**

```bash
./gradlew clean assembleDebug
```

Expected: BUILD SUCCESSFUL. If a dep was missed, the compiler will name it — restore that single dep, investigate why it's still needed, and either remove the use or add a `// kept because: <reason>` comment.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "chore: remove unused RxJava and Room extension dependencies

After the Kotlin migration, rxjava3, rxandroid, room-rxjava3,
room-guava, room-paging, and datastore-preferences-rxjava3 are no
longer referenced anywhere in the source tree."
```

---

### Task 4.2: Delete leftover .java files and verify

**Files:**
- Delete: any remaining `.java` files in `app/src/main/java/`
- Delete: `app/src/test/java/dev/thestbar/tunify/ExampleUnitTest.java`
- Delete: `app/src/androidTest/java/dev/thestbar/tunify/ExampleInstrumentedTest.java`

- [ ] **Step 1: List all remaining .java files in the app's source tree**

```bash
find app/src -name "*.java"
```

Expected output: only the two auto-generated `Example*Test.java` files (if any other production `.java` file is listed, stop — the Kotlin migration missed something).

- [ ] **Step 2: Delete the test stubs**

```bash
rm app/src/test/java/dev/thestbar/tunify/ExampleUnitTest.java 2>/dev/null
rm app/src/androidTest/java/dev/thestbar/tunify/ExampleInstrumentedTest.java 2>/dev/null
```

- [ ] **Step 3: Verify no .java files remain**

```bash
find app/src -name "*.java"
```

Expected: empty output.

- [ ] **Step 4: Full build + tests**

```bash
./gradlew clean assembleDebug test
```

Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "chore: delete leftover Java test stubs

App source tree is now pure Kotlin."
```

---

### Task 4.3: Housekeeping (README, project rename, version bump)

**Files:**
- Modify: `README.md`
- Modify: `app/build.gradle.kts`
- Modify: `settings.gradle.kts`

- [ ] **Step 1: Bump version in `app/build.gradle.kts`**

Find:

```kotlin
versionCode = 2
versionName = "1.1"
```

Replace with:

```kotlin
versionCode = 3
versionName = "2.0"
```

- [ ] **Step 2: Update the Gradle project name in `settings.gradle.kts`**

Find:

```kotlin
rootProject.name = "JunkieTuner"
```

Replace with:

```kotlin
rootProject.name = "Tunify"
```

- [ ] **Step 3: Update `README.md`**

Add or update the language mention. The exact change depends on README content — find the existing "Built with" / "Tech" / "Language" section (if any) and update it to mention "Kotlin". If no such section exists, append a short "Tech" section:

```markdown
## Tech

- Kotlin (100% — migrated from Java in May 2026)
- Android Gradle Plugin 8.7 / SDK 36 / Java 21
- AndroidX (ViewBinding, Lifecycle, DataStore, Room with KSP)
- kotlinx.coroutines (Flow for reactive streams)
- Material 3
```

- [ ] **Step 4: Final verification grep**

```bash
git grep -i 'rxjava\|rxandroid\|asynctask\|junkiedan\|junkietuner' app/src docs/ README.md gradle/ *.kts
```

Expected: no matches. If `junkietuner` appears in `docs/superpowers/specs/` historical files mentioning the rename, that's acceptable — those are historical.

- [ ] **Step 5: Full build + tests one more time**

```bash
./gradlew clean assembleDebug test
```

Expected: BUILD SUCCESSFUL, all tests pass.

- [ ] **Step 6: Smoke test (manual, one last time)**

Repeat the 12-step smoke test from Task 3.8 step 5. All should pass.

- [ ] **Step 7: Commit**

```bash
git add -A
git commit -m "chore: bump to v2.0, rename Gradle project to Tunify, README tech section

Reflects the major migration from Java to Kotlin."
```

- [ ] **Step 8: Merge the feature branch**

After confirming everything works:

```bash
git checkout main
git merge --no-ff kotlin-migration -m "merge: complete Kotlin migration"
```

(Alternatively, open a PR via `gh pr create` if you prefer the review-then-merge flow.)

---

## Done

All 21 Java files in `app/src/main/` are Kotlin. ViewBinding is adopted across all fragments and the activity. RxJava and AsyncTask are eliminated. The package is `dev.thestbar.tunify`. Pure-logic tests cover Yin, NoteDetection, NotesStructure, Note, GuitarTuning, Tuning, and TuningHandler.
