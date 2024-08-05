plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.dagger.hilt)
    id ("io.realm.kotlin")
    id ("kotlin-kapt")
}

android {
    namespace = "com.yaropaul.write"
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
        // Compatibility with Api level 24
        isCoreLibraryDesugaringEnabled = true
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
    // Dagger Hilt
    implementation (libs.hilt.android)
    kapt (libs.hilt.compiler)
    implementation (libs.androidx.hilt.navigation.compose)
    // Realm with Device Sync and using coroutines
    implementation (libs.library.sync)
    implementation (libs.kotlinx.coroutines.core)
    // Firebase
    implementation (libs.firebase.auth)
    implementation (libs.firebase.storage.ktx)
    // Coil
    implementation (libs.coil.compose)
    // Pager - Accompanist
    implementation (libs.accompanist.pager)
    // Date-Time Picker
    implementation (libs.core)
    // CALENDAR
    implementation (libs.calendar)
    // CLOCK
    implementation (libs.clock)
    // Desugar JDK use for compatibility date with api level 24
    coreLibraryDesugaring (libs.desugar.jdk.libs)
    // Modularization
    implementation(project(":core:ui"))
    implementation(project(":core:util"))
    implementation(project(":data:mongo"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}