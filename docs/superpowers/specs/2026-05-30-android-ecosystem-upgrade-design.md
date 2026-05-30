# Android Ecosystem Upgrade — Design Spec

**Date:** 2026-05-30
**Project:** Tunify (com.junkiedan.junkietuner)
**Scope:** Upgrade build toolchain, SDK levels, dependencies, and build file format to modern Android standards. Precursor to a full Kotlin migration.

---

## Overview

The project is a Java-based Android guitar tuner app. The build infrastructure is ~3 years behind: Gradle 7.5, AGP 7.4.0, Kotlin 1.7.21, compileSdk 33, and Groovy DSL build scripts. This upgrade brings everything to current standards and sets up a clean foundation for the subsequent Kotlin migration.

---

## 1. Build Toolchain

All three core build tools are upgraded to their latest stable versions at implementation time.

| Tool | Current | Target |
|------|---------|--------|
| Gradle wrapper | 7.5 | 8.x latest stable |
| Android Gradle Plugin (AGP) | 7.4.0 | 8.x latest stable |
| Kotlin | 1.7.21 | 2.1.x latest stable |

**Prerequisites verified:** Java 21 (OpenJDK) is the system default; JBR 17 also installed via Android Studio. Both exceed the Java 17 minimum required by AGP 8.x.

---

## 2. SDK & Java Compatibility

| Setting | Current | Target |
|---------|---------|--------|
| `compileSdk` | 33 | 36 |
| `targetSdk` | 33 | 36 |
| `minSdk` | 24 | 24 (unchanged) |
| `sourceCompatibility` | `VERSION_1_8` | `VERSION_21` |
| `targetCompatibility` | `VERSION_1_8` | `VERSION_21` |
| `kotlinOptions.jvmTarget` | not set | `"21"` |

`minSdk` stays at 24 (Android 7.0, ~97% device coverage). Targeting API 36 enforces edge-to-edge display and predictive back gesture handling — these are visual adjustments, not crashes, and will be verified after the upgrade.

---

## 3. Build File Migration — Groovy DSL → Kotlin DSL + Version Catalog

### 3a. File renames

| Before | After |
|--------|-------|
| `build.gradle` (root) | `build.gradle.kts` |
| `app/build.gradle` | `app/build.gradle.kts` |
| `settings.gradle` | `settings.gradle.kts` |

### 3b. Version catalog

A `gradle/libs.versions.toml` file is introduced to centralize all dependency and plugin versions. All build files reference it via type-safe `libs.*` accessors (e.g., `libs.androidx.appcompat` instead of a hardcoded string).

### 3c. Root build file simplification

The legacy `buildscript {}` block with the Kotlin plugin classpath is removed. The root `build.gradle.kts` uses only the modern `plugins {}` block with `apply(false)` declarations for all plugins.

---

## 4. Dependency Upgrades

All dependencies bumped to latest stable at implementation time. Approximate targets:

| Library | Current | Target |
|---------|---------|--------|
| `androidx.appcompat` | 1.6.1 | 1.7.x |
| `com.google.android.material` | 1.9.0 | 1.12.x |
| `androidx.constraintlayout` | 2.1.4 | 2.2.x |
| `androidx.room` (all artifacts) | 2.5.2 | 2.7.x |
| `androidx.datastore-preferences` | 1.0.0 | 1.1.x |
| `io.reactivex.rxjava3:rxjava` | 3.1.5 | 3.1.x latest |
| `io.reactivex.rxjava3:rxandroid` | 3.0.2 | 3.0.x latest |
| JUnit / Espresso | minor | latest stable |
| `com.github.wendykierp:JTransforms` | 3.1 | latest available |
| `com.github.anastr:speedviewlib` | 1.6.1 | latest available |

### 4a. JFreeChart → MPAndroidChart

`org.jfree:jfreechart` (1.5.4) is a desktop Java library that uses AWT/Swing classes unavailable on Android. It is removed and replaced with **MPAndroidChart** (`com.github.PhilJay:MPAndroidChart`), a purpose-built Android charting library.

**Implementation steps:**
1. Audit all usages of JFreeChart in the app source to understand what chart types are rendered.
2. Replace JFreeChart imports and chart construction code with MPAndroidChart equivalents.
3. Verify charts render correctly on a device/emulator.

---

## 5. Cleanup

- **Delete `mylibrary/`** — the module is already excluded from `settings.gradle` so it has no effect on the build. It is removed from disk entirely to avoid confusion.
- **`settings.gradle.kts`** continues to include only `:app`.

---

## Out of Scope

- Kotlin migration of app source code (`.java` → `.kt`) — this is the next planned phase after this upgrade is complete.
- Raising `minSdk` above 24.
- UI redesign or feature changes.
- Navigation component migration or other architectural refactors.

---

## Verification Checklist

After all changes are applied:
- [ ] Project builds successfully (`./gradlew assembleDebug`)
- [ ] All existing tests pass (`./gradlew test`)
- [ ] App installs and launches on an emulator targeting API 36
- [ ] App installs and launches on an emulator targeting API 24 (min supported)
- [ ] Tuner functionality works (audio recording, pitch detection, note display)
- [ ] Charts render correctly (post MPAndroidChart replacement)
- [ ] Room database migrations work (no data loss on upgrade)
- [ ] Edge-to-edge display looks acceptable
