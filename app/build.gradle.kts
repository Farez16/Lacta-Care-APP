plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")

    // --- CAMBIO: Quitamos kapt y ponemos ksp ---
    alias(libs.plugins.ksp)
    // id("kotlin-kapt") // Eliminado porque da error con Kotlin 2.0

    // --- HILT: Sin poner versión (la hereda del build.gradle del proyecto) ---
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.lactacare"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.lactacare"
        minSdk = 26
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    // El bloque kapt { ... } se ha eliminado porque ya no usamos kapt.
}

dependencies {
    // --- Tus dependencias existentes ---
    implementation(libs.androidx.material3)
    implementation(libs.play.services.cast.framework)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.core.i18n)
    implementation(libs.androidx.remote.creation.core)
    implementation(libs.places)

    val composeBom = platform("androidx.compose:compose-bom:2024.10.00")
    implementation(composeBom)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.foundation)
    implementation(libs.foundation)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // UI Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Navegación
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
    implementation("androidx.navigation:navigation-compose:2.8.2")

    // Network / API
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Imágenes
    implementation("io.coil-kt:coil-compose:2.6.0")

    // Security & Auth
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Iconos
    implementation("androidx.compose.material:material-icons-extended:1.6.0")

    // --- HILT (Inyección de Dependencias) ---
    implementation("com.google.dagger:hilt-android:2.55")

    // --- CAMBIO IMPORTANTE: Usamos ksp en vez de kapt ---
    ksp("com.google.dagger:hilt-android-compiler:2.55")

    // --- Hilt para Compose ---
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("com.squareup.retrofit2:converter-scalars:2.9.0")

    // PLAY SERVICES (UBICACION)
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // --- AGREGADO: Google Maps Compose ---
    implementation("com.google.maps.android:maps-compose:4.3.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Pull to Refresh
    implementation("com.google.accompanist:accompanist-swiperefresh:0.32.0")
}