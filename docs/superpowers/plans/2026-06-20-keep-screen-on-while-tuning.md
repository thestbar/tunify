# Keep Screen On While Tuning — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prevent the device display from sleeping while the Tuner is actively listening (`TunerUiState.isTuning == true`), and release the wake-lock as soon as the user mutes, navigates away, or backgrounds the app.

**Architecture:** Add a tiny reusable `KeepScreenOn(enabled: Boolean)` Compose helper that toggles `LocalView.current.keepScreenOn` inside a `DisposableEffect`. Invoke it once from `TunerScreen` passing `state.isTuning`. The wake-lock lives in the UI layer (a UI concern), is reactive to existing state, and is auto-released on dispose. No `MainActivity`, manifest, ViewModel, or navigation changes.

**Tech Stack:** Jetpack Compose (Material 3), Kotlin, Android `View.keepScreenOn` (a thin wrapper over `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` — no permission required).

**Spec:** `docs/superpowers/specs/2026-06-20-keep-screen-on-while-tuning-design.md`

## Note on automated testing

This plan skips automated tests deliberately. The project currently has unit tests for pure-Kotlin code (`algorithms`, `notes`, `data`) but **no Compose UI test infrastructure** — no `compose.ui.test` dependency, no `androidTest` source set. The single behavior being added is the assignment `view.keepScreenOn = enabled`, which is a one-line passthrough to an Android framework property. A UI test would exercise the framework, not our logic, and would require adding the entire `androidx.compose.ui:ui-test-junit4` + instrumented test runner setup — a scope explosion disproportionate to a 6-line composable.

The spec calls for manual verification (Task 3 below), which is the appropriate test for behavior that exists only at the platform window-flag layer.

---

## File Structure

| Path | Action | Responsibility |
|---|---|---|
| `app/src/main/java/dev/thestbar/tunify/ui/util/KeepScreenOn.kt` | **Create** | Reusable composable that toggles `View.keepScreenOn` based on a boolean, with automatic cleanup on dispose. |
| `app/src/main/java/dev/thestbar/tunify/ui/screens/TunerScreen.kt` | **Modify** | Invoke `KeepScreenOn(enabled = state.isTuning)` once at the top of the `TunerScreen` composable body. |

The new file lives under `ui/util/` — a new subpackage. No existing `util` subpackage under `ui/` exists yet (current `util/` is at the package root for non-UI helpers like `algorithms` and `notes`); `ui/util/` is the natural home for Compose-only helpers that aren't screens, components, themes, or navigation.

---

### Task 1: Add the `KeepScreenOn` composable

**Files:**
- Create: `app/src/main/java/dev/thestbar/tunify/ui/util/KeepScreenOn.kt`

- [ ] **Step 1: Create the file with the composable**

Create `app/src/main/java/dev/thestbar/tunify/ui/util/KeepScreenOn.kt` with the following contents (exactly):

```kotlin
package dev.thestbar.tunify.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView

@Composable
fun KeepScreenOn(enabled: Boolean) {
    val view = LocalView.current
    DisposableEffect(enabled) {
        view.keepScreenOn = enabled
        onDispose { view.keepScreenOn = false }
    }
}
```

Notes for the implementer:
- `LocalView.current` returns the host `android.view.View` for the current composition (Compose composes into an `AndroidComposeView`).
- The `DisposableEffect(enabled)` key means: whenever `enabled` changes, the effect's `onDispose` runs (clearing the flag) and then the effect re-runs (setting the flag to the new value). When the composable leaves composition entirely, `onDispose` runs too and the flag is cleared.
- `View.keepScreenOn = true` sets `FLAG_KEEP_SCREEN_ON` on the window; setting it to `false` clears the flag. No permission required.

- [ ] **Step 2: Build the module to verify the file compiles**

Run from the project root:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`. If you see "unresolved reference: LocalView", confirm the import is `androidx.compose.ui.platform.LocalView` (not `androidx.compose.ui.LocalView`).

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/ui/util/KeepScreenOn.kt
git commit -m "feat: add KeepScreenOn composable helper"
```

---

### Task 2: Wire `KeepScreenOn` into `TunerScreen`

**Files:**
- Modify: `app/src/main/java/dev/thestbar/tunify/ui/screens/TunerScreen.kt`

The current `TunerScreen` composable (`TunerScreen.kt:73-107`) begins like this:

```kotlin
@Composable
fun TunerScreen(
    state: TunerUiState,
    onToggleTuning: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedStringIndex by remember { mutableStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TunerHeader(isTuning = state.isTuning, onToggle = onToggleTuning)
        // ...
```

- [ ] **Step 1: Add the import**

In `TunerScreen.kt`, add this import alongside the existing `dev.thestbar.tunify.*` imports (the file already imports `dev.thestbar.tunify.R` at line 27 and `dev.thestbar.tunify.data.viewmodels.TunerUiState` at line 53, so place it near those for consistency):

```kotlin
import dev.thestbar.tunify.ui.util.KeepScreenOn
```

- [ ] **Step 2: Invoke `KeepScreenOn` at the top of the composable body**

Inside the `TunerScreen` composable body, insert the call as the very first statement — before `var selectedStringIndex by remember { ... }`. The relevant section should look exactly like this after the change:

```kotlin
@Composable
fun TunerScreen(
    state: TunerUiState,
    onToggleTuning: () -> Unit,
    modifier: Modifier = Modifier
) {
    KeepScreenOn(enabled = state.isTuning)

    var selectedStringIndex by remember { mutableStateOf(0) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TunerHeader(isTuning = state.isTuning, onToggle = onToggleTuning)
        // ... rest of body unchanged
```

Do not modify any other part of `TunerScreen.kt`.

- [ ] **Step 3: Build the module to verify it compiles**

Run from the project root:

```bash
./gradlew :app:compileDebugKotlin
```

Expected: `BUILD SUCCESSFUL`. If you see "unresolved reference: KeepScreenOn", confirm Task 1 was completed and the import added in Step 1 above matches the package declaration of the new file.

- [ ] **Step 4: Run the existing unit-test suite to confirm nothing else broke**

Run from the project root:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: `BUILD SUCCESSFUL`, all existing tests in `app/src/test/` pass. None of those tests touch `TunerScreen` or Compose — this is a regression safety net only.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/dev/thestbar/tunify/ui/screens/TunerScreen.kt
git commit -m "feat: keep screen on while tuner is listening"
```

---

### Task 3: Manual verification on a device or emulator

**Files:** none (verification only).

This task is performed by a human running the app and observing display behavior. Cannot be performed by an automated agent.

Pre-requisite for this task: the system display timeout on the test device should be set to a short value (Settings → Display → Screen timeout → 30 seconds is ideal). The default on most devices is 30s or 1 min.

- [ ] **Step 1: Install the debug APK on a device or emulator**

```bash
./gradlew :app:installDebug
```

Expected: `BUILD SUCCESSFUL`, app installed under the launcher name "Tunify".

- [ ] **Step 2: Verify screen stays awake while tuning**

1. Launch Tunify; you land on the Tuner tab.
2. Flip the Tuning switch in the header to **on** (label changes from "Muted" to "Tuning"). Grant the microphone permission if prompted.
3. Set the phone down. Do not touch the screen.
4. Wait at least the system display timeout + 10 seconds.

Expected: the display remains fully bright the entire time and does not dim or sleep.

- [ ] **Step 3: Verify screen sleeps when muted**

1. Still on the Tuner tab. Flip the Tuning switch **off** (label returns to "Muted").
2. Set the phone down. Do not touch the screen.
3. Wait at least the system display timeout + 10 seconds.

Expected: the display dims and sleeps on the normal system timeout.

- [ ] **Step 4: Verify screen sleeps when navigating away mid-session**

1. Flip the Tuning switch **on** again.
2. Tap the **Tunings** tab in the bottom navigation bar (the switch state in the Tuner tab is unimportant here — we're confirming the wake-lock releases when `TunerScreen` leaves composition).
3. Set the phone down.
4. Wait at least the system display timeout + 10 seconds.

Expected: the display dims and sleeps on the normal system timeout, despite the Tuning switch being on the last time the Tuner tab was visible.

- [ ] **Step 5: Verify screen sleeps when app is backgrounded mid-session**

1. Return to the Tuner tab. Confirm the Tuning switch is **on**.
2. Press the device home button or swipe up to background the app.
3. Set the phone down.
4. Wait at least the system display timeout + 10 seconds.

Expected: the display dims and sleeps on the normal system timeout (Android clears the flag automatically when the View detaches).

- [ ] **Step 6: No commit**

This task records observations only — no files changed.

---

## Done

After all three tasks are complete, the feature is shipped. The `main` branch will have two new commits (one per code task) and the spec already committed in the previous step. No follow-up work is needed unless manual verification surfaces an issue.
