# Compose Migration Design

## Goal

Replace every Fragment, XML layout, and View-based component with idiomatic Jetpack Compose screens, add a full Material3 dark + light theme, and add a theme-preference setting (System / Light / Dark).

## Strategy

Full "rip and replace": delete all Fragments and XML layouts in one branch. No hybrid period. All new screens are stateless composables driven by existing ViewModels (unchanged except for a new `TunerUiState` data class and a new `ThemeViewModel`).

---

## Architecture

```
MainActivity  (setContent { TunifyApp() })
└── TunifyApp (reads ThemePreference, provides TunifyTheme)
    └── TunifyNavHost (NavHost, 4 destinations)
        ├── TunerScreen
        ├── TuningsScreen
        ├── SettingsScreen
        └── InfoScreen
```

- `TunifyTheme` wraps `MaterialTheme` with custom `darkColorScheme` / `lightColorScheme`.
- Navigation uses Navigation Compose (`NavHost` + `NavController`). The bottom nav bar is a `NavigationBar` composable inside a `Scaffold`.
- All screens are stateless — they receive `UiState` and lambda callbacks.

---

## Theme

### Color palette (existing custom colors, new M3 role assignments)

| M3 Role | Dark value | Light value |
|---|---|---|
| `background` | `#231F20` (raisin black) | `#EFE6DD` (linen) |
| `onBackground` | `#EFE6DD` | `#231F20` |
| `surface` | `#564D4E` (wenge) | `#D4C5BF` (linen-wenge mix, new) |
| `onSurface` | `#EFE6DD` | `#231F20` |
| `primary` | `#BB4430` (persian red) | `#BB4430` (same) |
| `onPrimary` | `#EFE6DD` | `#EFE6DD` |
| `secondary` | `#7EBDC2` (verdigris) | `#7EBDC2` |
| `onSurfaceVariant` | `#8C7D7F` (taupe gray) | `#564D4E` (wenge) |
| `tertiary` | `#F3DFA2` (vanilla) | `#F3DFA2` |

`#D4C5BF` is the only new color — a midpoint between linen and wenge for light surface.

### ThemePreference

A new `THEME_PREFERENCE` key in DataStore (string enum: `SYSTEM` / `LIGHT` / `DARK`). `TunifyTheme` receives the resolved `darkTheme: Boolean`:

```kotlin
darkTheme = when (pref) {
    ThemePreference.DARK   -> true
    ThemePreference.LIGHT  -> false
    ThemePreference.SYSTEM -> isSystemInDarkTheme()
}
```

---

## Navigation

Navigation Compose with a `Scaffold` bottom bar:

- Destinations: `tuner`, `tunings`, `settings`, `info`
- Start destination: `tuner`
- `NavigationBar` with 4 `NavigationBarItem` entries
- Back stack managed by `NavController` with `saveState = true` / `restoreState = true` (preserves scroll and ViewModel state across tab switches)

---

## Screens

### TunerScreen

- **Toggle**: Full-width pill-shaped `Button` / `OutlinedButton` pair (Tuning On / Muted). Replaces the `MaterialSwitch`. State driven by `isTuning` from `TunerUiState`.
- **Note display**: Large `Text` badge (detected note name) + smaller cents readout below.
- **Speedometer**: `AndroidView` wrapper (`SpeedViewComposable`) around the existing `SpeedView` third-party view — reused as-is.
- **String chips**: Horizontal `LazyRow` of `SuggestionChip`s showing the strings of the active tuning.
- **Current tuning label**: Shown above the chips.

### TuningsScreen

- **Search**: M3 `SearchBar` composable (persistent, not dialog). Query flows to `TuningViewModel.setSearchQuery()`.
- **Sort**: Horizontal `LazyRow` of `FilterChip`s: Default, Name A→Z, Name Z→A, Newest First. Selected chip matches `TuningViewModel.sortOrder`. On tap, calls `TuningViewModel.setSortOrder()`.
- **List**: `LazyColumn` of tuning rows. Each row uses `SwipeToDismissBox` — swipe left to delete with an undo `Snackbar`.
- **FAB**: `ExtendedFloatingActionButton` ("+ Add Tuning") that collapses to icon-only on list scroll.
- **Add / Edit sheet**: `ModalBottomSheet` with an `OutlinedTextField` (tuning name) and one `ExposedDropdownMenuBox` per string (note selector). Shared for add and edit flows.

### SettingsScreen

- **Theme row**: M3 `SegmentedButton` row with three options: System / Light / Dark. Writes `ThemePreference` to DataStore via `ThemeViewModel`.
- **Mute preference row**: `SwitchPreferenceRow` composable (label + `Switch`), reads/writes existing DataStore key.

### InfoScreen

- Static composable: app version, author name + link (thestbar.dev), GitHub link.
- Uses `LocalUriHandler` to open links.

---

## Data Flow

```
DataStore ──► ThemeViewModel.themePreference (StateFlow)
                    │
                    ▼
             TunifyApp (collectAsStateWithLifecycle)
                    │
                    ▼
             TunifyTheme(darkTheme = ...)

TunerViewModel.uiState ──► TunerScreen (collectAsStateWithLifecycle)
TuningViewModel.filteredTunings + sortOrder ──► TuningsScreen
ThemeViewModel.themePreference ──► SettingsScreen
```

- All screens collect with `collectAsStateWithLifecycle()` (lifecycle-aware, no leaks).
- ViewModels are retrieved at the NavHost level and passed down as parameters (no `viewModel()` inside individual screen composables — keeps them testable).

---

## Files

### Create

| Path | Purpose |
|---|---|
| `ui/theme/Color.kt` | Named color constants |
| `ui/theme/TunifyTheme.kt` | `MaterialTheme` wrapper, dark + light color schemes |
| `ui/screens/TunerScreen.kt` | Tuner composable + sub-composables |
| `ui/screens/TuningsScreen.kt` | Tunings composable + sub-composables |
| `ui/screens/SettingsScreen.kt` | Settings composable |
| `ui/screens/InfoScreen.kt` | Info composable |
| `ui/navigation/TunifyNavHost.kt` | `NavHost` with `Scaffold` + `NavigationBar` |
| `ui/MainActivity.kt` | Replaces existing `MainActivity` — sets `setContent { TunifyApp() }` |
| `data/preferences/ThemePreference.kt` | Enum + DataStore key + read/write helpers |
| `data/viewmodels/ThemeViewModel.kt` | Exposes `themePreference: StateFlow<ThemePreference>` |

### Modify

| Path | Change |
|---|---|
| `data/viewmodels/TunerViewModel.kt` | Add `TunerUiState` data class, expose single `uiState: StateFlow<TunerUiState>` |
| `gradle/libs.versions.toml` + `app/build.gradle.kts` | Add Compose BOM, Navigation Compose, `activity-compose`, `lifecycle-runtime-compose` |

### Delete

- All Fragment classes: `MainFragment`, `TuningsFragment`, `SettingsFragment`, `InfoFragment`
- All XML layouts: `fragment_main.xml`, `fragment_tunings.xml`, `fragment_settings.xml`, `fragment_info.xml`, `main_app_screen.xml`, `item_tuning.xml`
- `nav_graph.xml`, `bottom_navigation_menu.xml`
- XML drawables that become unused (`sort_icon.xml`, `search_icon.xml`, etc.)

---

## Dependencies to Add (Compose BOM ~2025.05)

```toml
compose-bom = "2025.05.01"
androidx-compose-bom
androidx-compose-ui
androidx-compose-ui-tooling-preview
androidx-compose-material3
androidx-activity-compose
androidx-navigation-compose
androidx-lifecycle-runtime-compose
```

---

## Out of Scope

- Animations beyond M3 defaults (transitions, shared element)
- Tablet / foldable adaptive layout
- Accessibility audit beyond default M3 semantics
- Unit tests for composables (screenshot tests)
