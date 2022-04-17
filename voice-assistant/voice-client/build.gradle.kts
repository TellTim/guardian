plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = AppConfigs.compileSdkVersion

    defaultConfig {
        applicationId = "cn.telltim.voice.client"
        minSdk = AppConfigs.minSdkVersion
        targetSdk = AppConfigs.targetSdkVersion
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
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = Versions.composeVersion
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    setFlavorDimensions(listOf("client"))
    productFlavors {
        create("demo") {
            isDefault = true
            dimension = "client"
            buildConfigField(
                "String", "CLIENT_NAME", "demo"
            )
            buildConfigField(
                "String", "CUSTOM_PREFIX", "test"
            )

        }
    }
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(KtDeps.kotlin)
    implementation(KtDeps.ktx)
    implementation(Deps.multidex)
    implementation(Deps.appCompat)
    implementation(Deps.constraintLayout)
    implementation(ThirdDeps.fastjson)
    testImplementation(Deps.TestDeps.junit)
    androidTestImplementation(Deps.TestDeps.extJunit)
    androidTestImplementation(Deps.TestDeps.espresso)
    debugImplementation(ThirdDeps.leakCanary)
    implementation(project(mapOf("path" to ":voice-assistant:voice-app:assistant-binder")))
    implementation(project(mapOf("path" to ":voice-assistant:voice-sdk")))
    implementation(project(mapOf("path" to ":logger")))
}