# Keep Screen On While Tuning — Design

## Problem

While using the tuner, the device screen turns off after the system inactivity timeout because the user is holding the guitar with both hands and not touching the phone. This interrupts the tuning workflow — comparable tuner apps prevent this by keeping the screen awake while the tuner is engaged.

## Scope

Keep the display awake **only while the Tuner is actively listening** — i.e. while `TunerUiState.isTuning == true`. Release the wake-lock as soon as the user mutes, navigates away from the Tuner tab, or backgrounds the app.

Out of scope: keeping the screen on in Tunings / Settings / Info screens, or while the Tuner tab is visible but muted.

## Approach

Use Android's `View.keepScreenOn` (a thin wrapper over `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON`) toggled from a small reusable composable. No permission required.

Three approaches were considered:

- **Reusable `KeepScreenOn(enabled)` composable using `LocalView.current.keepScreenOn`** — chosen. Tiny, no `Activity` casting, automatic cleanup on dispose, reusable.
- **Direct window-flag toggle inside `TunerScreen`** — rejected. Requires casting `LocalView.current.context` to `Activity`; not reusable; same behavior otherwise.
- **Expose `isTuning` to `MainActivity` and toggle the window flag there** — rejected. Most ceremony, no benefit; the screen owns the toggle, so it should own the wake-lock.

## Changes

### New file: `app/src/main/java/dev/thestbar/tunify/ui/util/KeepScreenOn.kt`

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

### Modified file: `app/src/main/java/dev/thestbar/tunify/ui/screens/TunerScreen.kt`

Add one call at the top of the `TunerScreen` composable body, before the existing `Column`:

```kotlin
KeepScreenOn(enabled = state.isTuning)
```

Plus the required import: `import dev.thestbar.tunify.ui.util.KeepScreenOn`.

### Not modified

- `MainActivity` — no activity-level changes; the wake-lock is owned by the screen.
- `AndroidManifest.xml` — `FLAG_KEEP_SCREEN_ON` requires no permission.
- `TunerViewModel` — the existing `state.isTuning` flow drives the toggle.
- Navigation — unchanged; composable disposal cleans up automatically.

## Behavior matrix

| Tuning switch | Visible screen | Display stays awake? |
|---|---|---|
| On | Tuner | Yes |
| Off (Muted) | Tuner | No — normal system timeout applies |
| On | Switched to Tunings / Settings / Info | No — `TunerScreen` leaves composition, flag cleared in `onDispose` |
| On | App backgrounded | No — the View detaches, Android clears the flag automatically |

## Testing

Manual verification only — no automated test, since this exercises platform window behavior:

1. Open the Tuner tab, flip the Tuning switch on, set the phone down. Confirm the display stays awake past the system inactivity timeout (default ~30s).
2. Flip the switch off. Confirm the display dims and sleeps on the normal timeout.
3. Flip the switch on, switch to the Tunings tab. Confirm the display dims on the normal timeout.
4. Flip the switch on, background the app (home button). Confirm the display dims on the normal timeout.

## Risk

Very low. `FLAG_KEEP_SCREEN_ON` is a well-trodden API; the wake-lock is scoped to a View and released automatically on detach. Worst-case failure (composable never disposes) is bounded by the View lifecycle — the OS clears the flag when the window goes away.
