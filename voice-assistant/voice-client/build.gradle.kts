plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = AppConfigs.compileSdkVersion

    defaultConfig {
        applicationId = "cn.telltim.voice.client"
        minSdk = AppConfigs.minSdkVersion
        targetSdk= AppConfigs.targetSdkVersion
        versionCode = AppConfigs.versionCode
        versionName = AppConfigs.versionName
        vectorDrawables {
            useSupportLibrary = true
        }

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(KtDeps.ktx)
    implementation(Deps.multidex)
    implementation(Deps.ComposeDeps.composeUI)
    implementation(Deps.ComposeDeps.composeMaterial)
    implementation(Deps.ComposeDeps.composeUIPreview)
    implementation(Deps.LifecycleDeps.lifecycleRuntimeKtx)
    implementation(Deps.ComposeDeps.activityCompose)
    testImplementation(Deps.TestDeps.junit)
    androidTestImplementation(Deps.TestDeps.extJunit)
    androidTestImplementation(Deps.TestDeps.espresso)
    androidTestImplementation(Deps.ComposeDeps.composeUiTest)
    debugImplementation(Deps.ComposeDeps.composeUiTooling)
    debugImplementation(ThirdDeps.leakCanary)
    implementation(project(mapOf("path" to ":voice-assistant:voice-sdk")))
    implementation(project(mapOf("path" to ":logger")))
}