# Kotlin Migration — Design

**Date:** 2026-05-30
**Status:** Approved, ready for implementation planning
**Scope:** Migrate all 21 Java source files in `app/src/main/java/` to idiomatic Kotlin, adopt ViewBinding, rename package to `com.thestbar.tunify`, and add pure-logic unit tests.

## Goals

1. Replace every `.java` file in `app/src/main/java/` with an idiomatic Kotlin equivalent.
2. Replace deprecated/problematic patterns with modern Kotlin idioms (Coroutines, Flow, proper `ViewModelProvider`, `data class`, `object`, `@Parcelize`).
3. Adopt ViewBinding throughout, eliminating `findViewById` calls.
4. Rename the package from `com.junkiedan.junkietuner` to `com.thestbar.tunify`.
5. Add a pure-logic unit test suite covering pitch detection, note structure, and tuning parsing.
6. Remove RxJava dependencies once no longer needed.

## Non-goals

- No XML layout changes (layouts stay XML; only the binding mechanism changes).
- No new features.
- No Room schema/version changes — column shape is unchanged.
- No instrumented/Espresso tests, no DAO tests, no mocking framework.
- No `applicationId` change to `com.tunify` (would imply ownership of `tunify.com`).
- No `versionCode` bump beyond the standard +1 / `versionName` 2.0.

## Decisions

| Decision | Choice | Rationale |
|---|---|---|
| Migration style | **Idiomatic Kotlin** | Coroutines/Flow replace RxJava entirely; proper `ViewModelProvider`; `data class`, `object`, `@Parcelize` everywhere applicable. |
| Adjacent improvements | **Adopt ViewBinding** | Pairs naturally with Kotlin migration; fragments are full of `findViewById`. |
| Package | **`com.thestbar.tunify`** | Follows reverse-DNS convention using the developer's handle; avoids implying ownership of `tunify.com`. |
| Verification | **Pure-logic unit tests** added during Phase 1 | The pitch detection logic is pure JVM code — easy, high-value tests. |
| Test scope | **Yin, NoteDetection, NotesStructure, Note, GuitarTuning, Tuning, TuningHandler** | Pure logic with no Android dependency. |
| Mocking | **None** | Test surface is pure functions; no mocks needed. |
| Sequencing | **Bottom-up by layer (Phase 0 → 4)** | Matches Java/Kotlin interop direction; allows test/commit checkpoints; isolates highest-risk changes to one layer. |
| Intermediate compilation | **Phases 2+3 commit individually on a single feature branch** that only merges when the whole build is green | Avoids throwaway shim code; trades clean main for clean diffs. |

## Architecture changes

### What stays the same

- XML resources (layouts, drawables, themes, strings).
- Room schema (table name, column names, types, primary key, database name `app_db`).
- Audio recording approach (`AudioRecord` + background thread).
- Yin pitch detection algorithm (port is behavior-bit-exact except for fixing the singleton-ignores-sampleRate bug).
- Bottom-navigation fragment-switching architecture.

### What changes

- **Concurrency model.** `AsyncTask` → `kotlinx.coroutines` + `viewModelScope`/`lifecycleScope`. RxJava `Flowable` → Kotlin `Flow`. `.blockingFirst()` on the main thread → suspending `.first()` inside `lifecycleScope.launch`.
- **`TuningViewModel` shape.** Manual static-method singleton → standard `AndroidViewModel` accessed via `by viewModels()` / `by activityViewModels()`.
- **`Yin` shape.** Manual singleton (buggy — silently ignores `sampleRate` after first construction) → instance class. Callers hold their own `Yin(sampleRate)`.
- **`JunkieTunerAppDatabase` rename** → `TunifyDatabase`. Database file name (`app_db`) and schema stay the same — no Room migration needed.
- **`GuitarTuning` serialization.** `implements Serializable` → `@Parcelize implements Parcelable`.
- **DAO return types.** `LiveData<List<Tuning>>` → `Flow<List<Tuning>>`. Write methods become `suspend fun`.

### Package layout after migration

```
app/src/main/java/com/thestbar/tunify/
├── core/
│   ├── PreferencesDataStoreHandler.kt   (object)
│   ├── RecordingRunnable.kt
│   ├── TuningAdapter.kt
│   ├── activities/
│   │   └── MainActivity.kt
│   └── fragments/
│       ├── AddTuningDialogFragment.kt
│       ├── InfoFragment.kt
│       ├── MainFragment.kt
│       ├── SettingsFragment.kt
│       └── TuningsFragment.kt
├── data/
│   ├── TuningHandler.kt                  (object)
│   ├── TuningRepository.kt
│   ├── dao/
│   │   └── TuningDao.kt                  (interface, suspend + Flow)
│   ├── databases/
│   │   └── TunifyDatabase.kt             (renamed)
│   ├── entities/
│   │   └── Tuning.kt                     (data class)
│   └── viewmodels/
│       └── TuningViewModel.kt            (AndroidViewModel)
└── util/
    ├── algorithms/
    │   ├── NoteDetection.kt
    │   ├── PitchDetectionAlgorithm.kt    (fun interface)
    │   └── Yin.kt                        (instance class)
    └── notes/
        ├── GuitarTuning.kt               (@Parcelize)
        ├── Note.kt                       (data class)
        └── NotesStructure.kt             (object)

app/src/test/java/com/thestbar/tunify/
├── data/
│   ├── TuningHandlerTest.kt
│   └── entities/
│       └── TuningTest.kt
├── util/
│   ├── algorithms/
│   │   ├── NoteDetectionTest.kt
│   │   └── YinTest.kt
│   └── notes/
│       ├── GuitarTuningTest.kt
│       ├── NoteTest.kt
│       └── NotesStructureTest.kt
```

## Phases

### Phase 0 — Build setup & one-time prep (1 commit)

1. Switch annotation processing to **KSP** (`com.google.devtools.ksp` plugin; swap `room-compiler` from `annotationProcessor` to `ksp`).
2. Add to version catalog: `kotlinx-coroutines-android`, `kotlinx-coroutines-test`, `androidx.lifecycle:lifecycle-viewmodel-ktx`, `androidx.lifecycle:lifecycle-runtime-ktx`, `androidx.fragment:fragment-ktx`, `androidx.activity:activity-ktx`.
3. Enable ViewBinding (`buildFeatures { viewBinding = true }`).
4. Apply `kotlin-parcelize` plugin.
5. Rename package: update `namespace` and `applicationId` in `app/build.gradle.kts`, move source folders `com/junkiedan/junkietuner/` → `com/thestbar/tunify/`, rewrite `package` declarations and imports in every `.java` file in this single commit. Update `AndroidManifest.xml` activity reference.

**Verification:** `./gradlew assembleDebug` succeeds, app installs and launches on emulator, smoke test (open each fragment) still works.

### Phase 1 — Pure logic layer (6 commits)

Each file: convert to idiomatic Kotlin and add its test file in the same commit.

1. **`Note`** → `data class Note(val name: String, val frequency: Double)`.
2. **`PitchDetectionAlgorithm`** → `fun interface`.
3. **`NotesStructure`** → Kotlin `object`. Lazy null-checks → `by lazy`. Behavior bit-exact (concert pitch 440 Hz, 96 notes A0–G#8).
4. **`Yin`** → instance `class Yin(private val sampleRate: Double) : PitchDetectionAlgorithm`. **Fixes existing bug** where the singleton silently ignored a different `sampleRate` after first construction. Java callers (still-Java fragments) update from `Yin.getInstance(44100)` to `Yin(44100.0)`.
5. **`NoteDetection`** → `class NoteDetection(private val allNotes: Array<Note>)` with `companion object` for `compareClosestNoteToTarget`, `getDifferentInCents`, `LOG2_TO_LOG10_CONVERSION_CONST`.
6. **`GuitarTuning`** → `@Parcelize class GuitarTuning(val tuningName: String, noteNames: Array<String>) : Parcelable`. Replace Java `assert` with `require(noteNames.size == 6)` in `init`.

**Tests added in Phase 1:**

- `NoteTest.kt` — equality, `toString`.
- `NotesStructureTest.kt` — `allNotes.size == 96`, `searchNote("A4").frequency == 440.0`, search returns null for unknown name, A0 = 27.5 Hz, G#8 ≈ 6644.88 Hz.
- `NoteDetectionTest.kt` — binary search boundaries (≤ A0, ≥ G#8, exact match, between two notes); cents calculation for known intervals (e.g., one semitone = 100 cents, one octave = 1200 cents).
- `YinTest.kt` — synthesize a pure sine wave at a known frequency (e.g., 440 Hz at 44100 Hz sample rate), assert `getPitch()` returns within ±1 Hz.
- `GuitarTuningTest.kt` — valid 6-note construction; unknown note name falls back to A0; `require()` rejects array of size != 6.

**Verification:** `./gradlew test` passes all new tests. App still builds and launches.

### Phase 2 — Data layer (7 commits)

**Highest-risk phase.** This is where `AsyncTask` → Coroutines, RxJava → Flow, manual singleton ViewModel → standard `AndroidViewModel`.

1. **`Tuning`** entity → `data class`. Properties `var` (Room requires mutability for `id`). Drop manual `toString` (data class generates it).
2. **`TuningDao`** → Kotlin `interface`. Write methods become `suspend fun`. Reads: `getAllTunings(): Flow<List<Tuning>>`; `getTuningById(id: Int): Flow<Tuning?>`.
3. **`JunkieTunerAppDatabase`** → renamed to **`TunifyDatabase`**. `abstract class` with standard Kotlin double-checked-locking singleton in `companion object`. Database file name `"app_db"` unchanged.
4. **`TuningRepository`** → Kotlin `class`. All `AsyncTask` inner classes deleted. Methods become `suspend fun` delegating to DAO. Read methods return `Flow`.
5. **`TuningViewModel`** → full rewrite. `class TuningViewModel(application: Application) : AndroidViewModel(application)`. Manual singleton dropped. Exposes `val allTunings: StateFlow<List<Tuning>>` via `stateIn(viewModelScope, ...)`; write methods launch into `viewModelScope`.
6. **`TuningHandler`** → Kotlin `object`. `resetDatabaseValuesToDefault` becomes `suspend fun` taking a `TuningRepository`. ~115 default tunings extracted to a private `val DEFAULT_TUNINGS = listOf(...)` for cleanliness.
7. **`PreferencesDataStoreHandler`** → Kotlin `object`. Reads return `Flow<Boolean>` (was RxJava `Flowable<Boolean>`). Writes become `suspend fun`. Uses standard `DataStore<Preferences>` extension property + `data.map { }` + `dataStore.edit { }`. Eliminates the `.blockingFirst()` main-thread calls in `MainActivity`.

**Tests added in Phase 2** (bundled with the source file each one covers):

- `TuningTest.kt` (with the `Tuning` entity commit) — verify `notesFormatted()` produces `"E2  A2  D3  G3  B3  E4"` (double-space separator) from the canonical `"[E2,A2,D3,G3,B3,E4]"` input; check edge cases (empty bracket pair, single note, sharps).
- `TuningHandlerTest.kt` (with the `TuningHandler` commit) — `getNotesStringFromNotesArray()` and `getGuitarTuningFromTuning()` are pure round-trippable functions; verify a tuning string parses to the correct notes and the notes serialize back to the original string. `resetDatabaseValuesToDefault()` is not tested (it depends on a real repository).

**Verification at the end of Phase 2:** Because the Java UI in `app/src/main/java/com/thestbar/tunify/core/` still calls the old static `TuningViewModel.insert()` API and the old `Flowable`-returning DataStore methods, the **app module will not compile at the end of Phase 2 in isolation**. This is expected. The Phase 2 commits are reviewable independently, but the build only returns to green when Phase 3 rewires the UI callers. Final verification happens at end of Phase 3.

### Phase 3 — UI layer (8 commits)

Adopt ViewBinding throughout; rewire every caller of the data layer to the new Coroutines/Flow APIs.

1. **`RecordingRunnable`** → Kotlin `class` implementing `Runnable`. `@Volatile var shouldStop: Boolean` replaces Java volatile boolean.
2. **`TuningAdapter`** → Kotlin `RecyclerView.Adapter` with nested `ViewHolder(val binding: TuningRowBinding) : RecyclerView.ViewHolder(binding.root)`.
3. **`AddTuningDialogFragment`** → Kotlin `class` + ViewBinding via the standard lazy idiom (`_binding`/`binding` pair, nulled in `onDestroyView`). Static `TuningViewModel.insert()` calls replaced by `viewModel.insert()` via `by activityViewModels()`.
4. **`InfoFragment`** → Kotlin `class` + ViewBinding. Smallest fragment, simple warmup.
5. **`SettingsFragment`** → Kotlin `class` + ViewBinding. Replace `Flowable` subscriptions with `viewLifecycleOwner.lifecycleScope.launch { repeatOnLifecycle(STARTED) { PreferencesDataStoreHandler.getX(context).collect { } } }`. Writes via `lifecycleScope.launch { PreferencesDataStoreHandler.setX(...) }`.
6. **`TuningsFragment`** → Kotlin `class` + ViewBinding. Replace `viewModel.allTunings.observe(...)` with `Flow.collect` inside `repeatOnLifecycle(STARTED)`.
7. **`MainFragment`** → Kotlin `class` + ViewBinding. Largest non-trivial migration: audio recording lifecycle, SpeedView updates, real-time pitch detection. RxJava `Flowable` chain reading `IS_MUTED` becomes a `Flow` collection. `RecordingRunnable` callbacks happen off the main thread — UI updates via `withContext(Dispatchers.Main)` or a `Channel<ShortArray>` consumed on the main dispatcher.
8. **`MainActivity`** → Kotlin `class` + ViewBinding. Three `.blockingFirst()` calls in `initDatabase()`/`initSettings()` rewritten as `lifecycleScope.launch { val initialized = PreferencesDataStoreHandler.hasBeenInitialized(context).first(); if (!initialized) { ... } }`. Permission request stays as the existing `ActivityCompat.requestPermissions` flow.

**Verification at end of Phase 3** (first full app verification since Phase 1):

- `./gradlew assembleDebug` succeeds.
- `./gradlew test` passes (Phase 1 tests).
- Install on emulator + smoke test:
  1. App launches, MainFragment opens.
  2. Microphone permission prompt appears; grant it.
  3. Play a note / hum — needle moves, note name updates.
  4. Navigate to Tunings tab — list of ~115 tunings loads.
  5. Open add-tuning dialog — insert works.
  6. Settings tab — toggle each switch, force-quit and reopen, state persists.
  7. Info tab — privacy policy link works.
  8. Rotate device — ViewModel state survives.

### Phase 4 — Cleanup (3 commits)

1. **Remove dead deps** from `gradle/libs.versions.toml` and `app/build.gradle.kts`:
   - `io.reactivex.rxjava3:rxjava`
   - `io.reactivex.rxjava3:rxandroid`
   - `androidx.datastore:datastore-preferences-rxjava3`
   - `androidx.room:room-rxjava3`
   - `androidx.room:room-guava` (already unused in source)
   - `androidx.room:room-paging` (already unused in source)

2. **Delete leftover Java files.** `find app/src/main/java -name "*.java" -delete`. Also delete the auto-generated test stubs (`ExampleUnitTest.java`, `ExampleInstrumentedTest.java`).

3. **Final housekeeping.** README mention of "Written in Kotlin". `versionCode` 2 → 3, `versionName` "1.1" → "2.0".

**Verification at end of Phase 4:**

- `./gradlew clean assembleDebug test` succeeds.
- No `.java` files anywhere in `app/src/main/`.
- Smoke test (from Phase 3) passes once more.
- `git grep -i 'rxjava\|rxandroid\|asynctask\|junkiedan\|junkietuner'` returns nothing.

## Summary

| Phase | Scope | Commits | Risk |
|---|---|---|---|
| 0 | Build setup, package rename | 1 | Low |
| 1 | Pure logic (6 files + 5 tests) | 6 | Low |
| 2 | Data layer (7 files + 2 tests) | 7 | **High** |
| 3 | UI layer (8 files + ViewBinding) | 8 | Medium |
| 4 | Cleanup (deps, Java sweep, version) | 3 | Low |
| **Total** | **21 Java files → Kotlin + 7 test files + dep cleanup** | **~25 commits** | |

All work proceeds on a single feature branch. CI is expected to be red on intermediate Phase 2 commits — the branch only merges to `main` once the full build + smoke test passes at the end of Phase 3 (or, equivalently, after Phase 4 ships its cleanup).
