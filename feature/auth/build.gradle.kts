plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.realm.kotin)
}

android {
    namespace = "com.yaropaul.auth"
    compileSdk = ProjectConfig.compileSdk

    defaultConfig {
        minSdk = ProjectConfig.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = ProjectConfig.extensionVersion
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation (libs.androidx.ui.tooling.preview)
    // Firebase
    implementation (libs.firebase.auth)
    // Realm with Device Sync and using coroutines
    implementation (libs.library.sync)
    implementation (libs.kotlinx.coroutines.core)
    // Compose Navigation
    implementation (libs.androidx.navigation.compose)
    // One-Tap Compose
    implementation (libs.onetapcompose)
    // Message Bar Compose
    implementation (libs.messagebarcompose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Modularization
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
}