// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Make sure this is 8.4.0 or newer
    id("com.android.application") version "8.13.1" apply false
    id("com.android.library") version "8.13.1" apply false

    // IMPORTANT: Kotlin should be 2.0.0 or newer
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false

    // ADD THIS LINE:
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}