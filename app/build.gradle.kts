plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = AppConfigs.compileSdkVersion
    ndkVersion = AppConfigs.ndkVersion
    defaultConfig {
        applicationId = "cn.telltim.guardian"
        minSdk = AppConfigs.minSdkVersion
        targetSdk= AppConfigs.targetSdkVersion
        versionCode = AppConfigs.versionCode
        versionName = AppConfigs.versionName
        multiDexEnabled = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            abiFilters.addAll(AppConfigs.abiFilters.split(","))
        }
    }

    buildTypes {

        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            isDebuggable = false
        }
        getByName("debug") {
            isMinifyEnabled = false
            isDebuggable = true
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

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeVersion
    }
    packagingOptions {
        resources.excludes.add("META-INF/{AL2.0,LGPL2.1}")
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
    implementation(project(mapOf("path" to ":common")))
    implementation(project(mapOf("path" to ":common-java")))
    implementation(project(mapOf("path" to ":startup")))
    implementation(project(mapOf("path" to ":xcrash")))
    implementation(project(mapOf("path" to ":logger")))
    testImplementation(Deps.TestDeps.junit)
    androidTestImplementation(Deps.TestDeps.extJunit)
    androidTestImplementation(Deps.TestDeps.espresso)
    androidTestImplementation(Deps.ComposeDeps.composeUiTest)
    debugImplementation(Deps.ComposeDeps.composeUiTooling)
    debugImplementation(ThirdDeps.leakCanary)
}