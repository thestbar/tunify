# Android Ecosystem Upgrade Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade Tunify's build toolchain, SDK targets, build file format, and all dependencies to current Android standards as a clean foundation for the subsequent Kotlin migration.

**Architecture:** All changes are confined to build infrastructure and two minor source/layout updates (SwitchMaterial → MaterialSwitch, edge-to-edge insets). No application logic changes. The single-Activity + four-Fragment structure, Room database, DataStore, and RxJava stack are preserved as-is.

**Tech Stack:** Gradle 8.x, Android Gradle Plugin 8.x, Kotlin 2.x, Java 21, compileSdk/targetSdk 36, Kotlin DSL (`.gradle.kts`), Gradle version catalog (`libs.versions.toml`)

---

### Task 1: Delete the unused mylibrary module

`mylibrary/` exists on disk but is not included in `settings.gradle` and is not referenced from the app. It is safe to delete.

**Files:**
- Delete: `mylibrary/` (entire directory)

- [ ] **Step 1: Remove the directory**

```bash
rm -rf mylibrary
```

- [ ] **Step 2: Commit**

```bash
git add -A
git commit -m "chore: remove unused mylibrary module"
```

---

### Task 2: Create the version catalog

**Files:**
- Create: `gradle/libs.versions.toml`

Before writing this file, look up the latest stable versions at:
- AGP + Kotlin: https://developer.android.com/build/releases/gradle-plugin
- Gradle: https://gradle.org/releases/
- AndroidX libraries: https://developer.android.com/jetpack/androidx/versions

The versions below are known-stable starting points from late 2024. Update any that have newer stable releases.

- [ ] **Step 1: Create `gradle/libs.versions.toml`**

```toml
[versions]
agp = "8.7.0"
kotlin = "2.1.0"
appcompat = "1.7.0"
material = "1.12.0"
constraintlayout = "2.2.0"
room = "2.6.1"
datastorePreferences = "1.1.1"
rxjava3 = "3.1.9"
rxandroid = "3.0.2"
junit = "4.13.2"
androidxJunit = "1.2.1"
espresso = "3.6.1"
jtransforms = "3.1"
speedviewlib = "1.6.1"

[libraries]
androidx-appcompat = { group = "androidx.appcompat", name = "appcompat", version.ref = "appcompat" }
material = { group = "com.google.android.material", name = "material", version.ref = "material" }
androidx-constraintlayout = { group = "androidx.constraintlayout", name = "constraintlayout", version.ref = "constraintlayout" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxJunit" }
espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espresso" }
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
room-rxjava3 = { group = "androidx.room", name = "room-rxjava3", version.ref = "room" }
room-guava = { group = "androidx.room", name = "room-guava", version.ref = "room" }
room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }
room-paging = { group = "androidx.room", name = "room-paging", version.ref = "room" }
datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastorePreferences" }
datastore-preferences-rxjava3 = { group = "androidx.datastore", name = "datastore-preferences-rxjava3", version.ref = "datastorePreferences" }
rxjava3 = { group = "io.reactivex.rxjava3", name = "rxjava", version.ref = "rxjava3" }
rxandroid = { group = "io.reactivex.rxjava3", name = "rxandroid", version.ref = "rxandroid" }
jtransforms = { group = "com.github.wendykierp", name = "JTransforms", version.ref = "jtransforms" }
speedviewlib = { group = "com.github.anastr", name = "speedviewlib", version.ref = "speedviewlib" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
android-library = { id = "com.android.library", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

Note: `org.jfree:jfreechart` is intentionally omitted — it was declared as a dependency but never imported or used anywhere in the source code.

- [ ] **Step 2: Commit**

```bash
git add gradle/libs.versions.toml
git commit -m "chore: add Gradle version catalog"
```

---

### Task 3: Update the Gradle wrapper

AGP 8.7 requires Gradle 8.9 or higher. Use Gradle 8.11.1 (verify the latest stable at https://gradle.org/releases/ and update accordingly).

**Files:**
- Modify: `gradle/wrapper/gradle-wrapper.properties`

- [ ] **Step 1: Replace the contents of `gradle/wrapper/gradle-wrapper.properties`**

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.11.1-bin.zip
zipStorePath=wrapper/dists
zipStoreBase=GRADLE_USER_HOME
```

- [ ] **Step 2: Commit**

```bash
git add gradle/wrapper/gradle-wrapper.properties
git commit -m "chore: upgrade Gradle wrapper to 8.11.1"
```

---

### Task 4: Migrate settings.gradle → settings.gradle.kts

JitPack is added to the repositories because `com.github.anastr:speedviewlib` is a JitPack artifact that was previously resolving from cache but was never explicitly declared.

**Files:**
- Delete: `settings.gradle`
- Create: `settings.gradle.kts`

- [ ] **Step 1: Delete the old Groovy file**

```bash
rm settings.gradle
```

- [ ] **Step 2: Create `settings.gradle.kts`**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
rootProject.name = "JunkieTuner"
include(":app")
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: migrate settings.gradle to Kotlin DSL, add JitPack repo"
```

---

### Task 5: Migrate root build.gradle → build.gradle.kts

The legacy `buildscript {}` block with the Kotlin classpath entry is dropped. All plugins are declared via the version catalog.

**Files:**
- Delete: `build.gradle`
- Create: `build.gradle.kts`

- [ ] **Step 1: Delete the old Groovy file**

```bash
rm build.gradle
```

- [ ] **Step 2: Create `build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: migrate root build.gradle to Kotlin DSL"
```

---

### Task 6: Migrate app/build.gradle → app/build.gradle.kts

This is the main change: SDK bumped to 36, Java bumped to 21, all dependency references switched to version catalog accessors, and JFreeChart removed.

**Files:**
- Delete: `app/build.gradle`
- Create: `app/build.gradle.kts`

- [ ] **Step 1: Delete the old Groovy file**

```bash
rm app/build.gradle
```

- [ ] **Step 2: Create `app/build.gradle.kts`**

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.junkiedan.junkietuner"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.junkiedan.junkietuner"
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
}

dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    // Room
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.room.rxjava3)
    implementation(libs.room.guava)
    testImplementation(libs.room.testing)
    implementation(libs.room.paging)
    // DataStore
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.preferences.rxjava3)
    // RxJava
    implementation(libs.rxandroid)
    implementation(libs.rxjava3)
    // Audio DSP
    implementation(libs.jtransforms)
    implementation(libs.speedviewlib)
}
```

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "chore: migrate app/build.gradle to Kotlin DSL, bump SDK to 36 and Java to 21"
```

---

### Task 7: First build verification

- [ ] **Step 1: Run the debug build**

```bash
./gradlew assembleDebug 2>&1 | tail -60
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Address any build errors**

Common errors and fixes:

| Error | Fix |
|-------|-----|
| `Could not resolve com.github.anastr:speedviewlib` | JitPack missing — verify Task 4's `settings.gradle.kts` has the JitPack `maven` block |
| `AGP requires Gradle X.Y or higher` | Bump `distributionUrl` in `gradle/wrapper/gradle-wrapper.properties` to the required version |
| `Unresolved reference: libs` | `gradle/libs.versions.toml` is missing or malformed — re-check Task 2 |
| `Duplicate class kotlin.collections.*` | Add `implementation(kotlin("stdlib"))` exclusion — older transitive deps conflict with Kotlin 2.x stdlib. Fix: add `configurations.all { resolutionStrategy.force("org.jetbrains.kotlin:kotlin-stdlib:2.1.0") }` in `app/build.gradle.kts` |

- [ ] **Step 3: Run unit tests**

```bash
./gradlew test 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL` (only auto-generated stub tests exist)

- [ ] **Step 4: Commit any fixes**

```bash
git add -A
git commit -m "chore: fix build issues after toolchain upgrade"
```

---

### Task 8: Replace SwitchMaterial with MaterialSwitch

`SwitchMaterial` is deprecated in Material 3 (Material 1.6+) in favour of `MaterialSwitch`. It appears in two layouts and two Java files.

**Files:**
- Modify: `app/src/main/res/layout/fragment_main.xml`
- Modify: `app/src/main/res/layout/fragment_settings.xml`
- Modify: `app/src/main/java/com/junkiedan/junkietuner/core/fragments/MainFragment.java`
- Modify: `app/src/main/java/com/junkiedan/junkietuner/core/fragments/SettingsFragment.java`

- [ ] **Step 1: Replace in both layout files**

In `app/src/main/res/layout/fragment_main.xml` and `app/src/main/res/layout/fragment_settings.xml`, replace every occurrence of:

```xml
<com.google.android.material.switchmaterial.SwitchMaterial
```

with:

```xml
<com.google.android.material.materialswitch.MaterialSwitch
```

And every closing tag:

```xml
</com.google.android.material.switchmaterial.SwitchMaterial>
```

with:

```xml
</com.google.android.material.materialswitch.MaterialSwitch>
```

Run this to verify no occurrences remain:

```bash
grep -r "SwitchMaterial" app/src/main/res/
```

Expected: no output.

- [ ] **Step 2: Update MainFragment.java**

In `app/src/main/java/com/junkiedan/junkietuner/core/fragments/MainFragment.java`:

Replace:
```java
import com.google.android.material.switchmaterial.SwitchMaterial;
```
with:
```java
import com.google.android.material.materialswitch.MaterialSwitch;
```

Replace:
```java
private SwitchMaterial tuningSwitch = null;
```
with:
```java
private MaterialSwitch tuningSwitch = null;
```

- [ ] **Step 3: Update SettingsFragment.java**

In `app/src/main/java/com/junkiedan/junkietuner/core/fragments/SettingsFragment.java`:

Replace:
```java
import com.google.android.material.switchmaterial.SwitchMaterial;
```
with:
```java
import com.google.android.material.materialswitch.MaterialSwitch;
```

Replace:
```java
private SwitchMaterial lockTunerSwitch;
```
with:
```java
private MaterialSwitch lockTunerSwitch;
```

Replace:
```java
private SwitchMaterial loadLastMutedStateSwitch;
```
with:
```java
private MaterialSwitch loadLastMutedStateSwitch;
```

- [ ] **Step 4: Build to confirm no errors**

```bash
./gradlew assembleDebug 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/res/layout/fragment_main.xml \
        app/src/main/res/layout/fragment_settings.xml \
        app/src/main/java/com/junkiedan/junkietuner/core/fragments/MainFragment.java \
        app/src/main/java/com/junkiedan/junkietuner/core/fragments/SettingsFragment.java
git commit -m "chore: replace deprecated SwitchMaterial with MaterialSwitch"
```

---

### Task 9: Handle edge-to-edge display enforcement (API 35+)

Targeting API 36 triggers Android's mandatory edge-to-edge enforcement on devices running Android 15+. Without this fix, the `BottomNavigationView` draws behind the system navigation bar on those devices.

**Files:**
- Modify: `app/src/main/java/com/junkiedan/junkietuner/core/activities/MainActivity.java`

- [ ] **Step 1: Add required imports to MainActivity.java**

Add these four imports at the top of the file alongside the existing imports:

```java
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
```

- [ ] **Step 2: Enable edge-to-edge in onCreate**

In `MainActivity.java`, add this line as the **very first line** inside `onCreate`, before `setContentView`:

```java
WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
```

So `onCreate` begins:

```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
    setContentView(R.layout.main_app_screen);
    // ... rest unchanged
```

- [ ] **Step 3: Apply bottom insets to the navigation bar**

At the end of `initBottomNavBar()`, after the `setOnItemSelectedListener` call, add:

```java
ViewCompat.setOnApplyWindowInsetsListener(bottomNavigationView, (v, insets) -> {
    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
    v.setPadding(v.getPaddingLeft(), v.getPaddingTop(), v.getPaddingRight(), systemBars.bottom);
    return insets;
});
```

- [ ] **Step 4: Build to confirm no errors**

```bash
./gradlew assembleDebug 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/junkiedan/junkietuner/core/activities/MainActivity.java
git commit -m "fix: apply edge-to-edge window insets for API 35+ enforcement"
```

---

### Task 10: Final verification

- [ ] **Step 1: Full clean build**

```bash
./gradlew clean assembleDebug 2>&1 | tail -30
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 2: Run all tests**

```bash
./gradlew test 2>&1 | tail -20
```

Expected: `BUILD SUCCESSFUL`

- [ ] **Step 3: Install on emulator and smoke test**

Launch an Android emulator (API 35 or 36) from Android Studio, then:

```bash
./gradlew installDebug
```

Verify the following manually:
- App launches without crash
- `BottomNavigationView` renders above the system navigation bar (not hidden behind it)
- Switching tabs works (Main, Tunings, Settings, Info all load)
- Main tab: microphone permission prompt appears on first run; SpeedView widget renders
- Settings tab: both toggle switches render and respond to tap
- Tunings tab: list of tunings loads

- [ ] **Step 4: Confirm git log is clean**

```bash
git log --oneline -10
```

All upgrade commits should be visible. No uncommitted changes:

```bash
git status
```

Expected: `nothing to commit, working tree clean`
