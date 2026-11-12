plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.notifii.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.notifii.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        buildConfigField("String", "PRO_PRODUCT", "\"notifii_pro\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    // Lets people pick Notifii's language separately from the phone's (Android 13+ settings).
    androidResources { generateLocaleConfig = true }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

ksp { arg("room.schemaLocation", "$projectDir/schemas") }

dependencies {
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.icons)
    debugImplementation(libs.compose.ui.tooling)

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.navigation.compose)
    implementation(libs.coroutines.android)

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.billing)

    testImplementation(libs.junit)
}

// The privacy promise is "no INTERNET permission". Fail the build if any library sneaks it back in.
tasks.matching { it.name.startsWith("process") && it.name.endsWith("MainManifest") }.configureEach {
    doLast {
        outputs.files.asFileTree.matching { include("**/AndroidManifest.xml") }.forEach { file ->
            check(!file.readText().contains("android.permission.INTERNET")) {
                "INTERNET permission found in merged manifest ($file). Notifii must stay offline."
            }
        }
    }
}
