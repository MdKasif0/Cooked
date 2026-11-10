plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
}

val enableFirebase = project.findProperty("cooked.enableFirebase")?.toString()?.toBoolean()
    ?: file("google-services.json").exists()

android {
    namespace = "com.cooked.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.cooked.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // RevenueCat public Google API key. Supply via -Pcooked.revenuecatApiKey=goog_... or the
        // COOKED_REVENUECAT_API_KEY env var. Empty means billing is disabled (purchases fail cleanly).
        val revenueCatApiKey = (project.findProperty("cooked.revenuecatApiKey")?.toString()
            ?: System.getenv("COOKED_REVENUECAT_API_KEY")
            ?: "").replace("\"", "")
        buildConfigField("String", "REVENUECAT_API_KEY", "\"$revenueCatApiKey\"")
    }

    signingConfigs {
        create("release") {
            val keystoreFile = file("../release.keystore")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("COOKED_KEYSTORE_PASSWORD") ?: "cooked_keystore_pass"
                keyAlias = System.getenv("COOKED_KEY_ALIAS") ?: "cooked_key"
                keyPassword = System.getenv("COOKED_KEY_PASSWORD") ?: "cooked_key_pass"
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Cooked Debug")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            resValue("string", "app_name", "Cooked")
            val releaseSigning = signingConfigs.getByName("release")
            signingConfig = if (releaseSigning.storeFile?.exists() == true) releaseSigning else signingConfigs.getByName("debug")
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

    buildFeatures {
        compose = true
        resValues = true
        buildConfig = true
    }

    sourceSets {
        getByName("test").assets.srcDirs("$projectDir/schemas")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}


detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("../detekt.yml"))
    buildUponDefaultConfig = true
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
}

dependencies {
    // AndroidX & Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation & Serialization
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization.json)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // DataStore & WorkManager
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)

    // Glance Widget
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Charts (Vico)
    implementation(libs.vico.compose.m3)

    // RevenueCat
    implementation(libs.revenuecat.purchases)

    // Google Mobile Ads (AdMob)
    implementation(libs.play.services.ads)

    // ML Kit Text Recognition
    implementation(libs.mlkit.text.recognition)

    // Google Play In-App Review & Flexible Update
    implementation(libs.play.review)
    implementation(libs.play.app.update)

    // LeakCanary (debug only)
    debugImplementation(libs.leakcanary.android)

    // Firebase (only attached if google-services.json exists or flag is set)
    if (enableFirebase) {
        implementation(platform(libs.firebase.bom))
        implementation(libs.firebase.analytics)
        implementation(libs.firebase.crashlytics)
    }

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.truth)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.room.testing)
    testImplementation("org.ow2.asm:asm:9.11.1")
    testImplementation("org.ow2.asm:asm-commons:9.11.1")
    testImplementation("org.ow2.asm:asm-util:9.11.1")
    testImplementation("org.ow2.asm:asm-tree:9.11.1")
}

configurations.all {
    resolutionStrategy {
        force("org.ow2.asm:asm:9.11.1")
        force("org.ow2.asm:asm-commons:9.11.1")
        force("org.ow2.asm:asm-util:9.11.1")
        force("org.ow2.asm:asm-tree:9.11.1")
    }
}
