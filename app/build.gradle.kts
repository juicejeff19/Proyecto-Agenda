plugins {
// REMOVE or COMMENT OUT the aliases:
    // alias(libs.plugins.android.application)
    // alias(libs.plugins.kotlin.android)

    // ADD THESE instead. Note there are NO version numbers here.
    // This tells Gradle: "Use the version we already defined in the root file"
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // This one is fine as is
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.proyectoagenda"

    // REPLACE the complex block with this:
    compileSdk = 36

    defaultConfig {
        // ... rest of defaultConfig is fine
        targetSdk = 36
        // ...
    }

    defaultConfig {
        applicationId = "com.example.proyectoagenda"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        compose = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.foundation)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // --- Compose Dependencies (Keep this set only once) ---
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // Extended Icons (Hamburger menu)
    implementation("androidx.compose.material:material-icons-extended")

    // ViewModel for Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
}