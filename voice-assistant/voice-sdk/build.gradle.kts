plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = AppConfigs.compileSdkVersion

    defaultConfig {
        minSdk = AppConfigs.minSdkVersion
        targetSdk = AppConfigs.targetSdkVersion

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(KtDeps.ktx)
    implementation(Deps.appCompat)
    implementation(Deps.materialDesign)
    testImplementation(Deps.TestDeps.junit)
    androidTestImplementation(Deps.TestDeps.extJunit)
    androidTestImplementation(Deps.TestDeps.espresso)
    implementation(project(mapOf("path" to ":logger")))
    implementation(project(mapOf("path" to ":common")))
    implementation(project(mapOf("path" to ":voice-assistant:voice-app:assistant-binder")))
}