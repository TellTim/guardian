plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = AppConfigs.compileSdkVersion

    defaultConfig {
        applicationId = "cn.telltim.voice.app"
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

    setFlavorDimensions(listOf("product", "vendor", "chip"))
    productFlavors {
        create("iflyos") {
            isDefault = true
            dimension = "product"
        }
        create("tuya") {
            isDefault = true
            dimension = "vendor"
        }
        create("a133") {
            isDefault = true
            dimension = "chip"
        }
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

    applicationVariants.all {
        // 编译类型
        val buildType = this.buildType.name
        val flavorName = this.flavorName
        outputs.all {
            // 判断是否是输入 apk 类型
            if (buildType == "debug"){
                if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                    this.outputFileName = "VoiceAssistant_${flavorName}_v${android
                        .defaultConfig.versionName}.${android.defaultConfig.versionCode}_debug.apk"
                }
            }else if (buildType == "release") {
                if (this is com.android.build.gradle.internal.api.ApkVariantOutputImpl) {
                    this.outputFileName = "VoiceAssistant_${flavorName}_v${android.defaultConfig.versionName}.${android.defaultConfig.versionCode}_release.apk"
                }
            }
        }
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
    implementation(project(mapOf("path" to ":voice-assistant:voice-app:assistant-binder")))
    testImplementation(Deps.TestDeps.junit)
    androidTestImplementation(Deps.TestDeps.extJunit)
    androidTestImplementation(Deps.TestDeps.espresso)
    androidTestImplementation(Deps.ComposeDeps.composeUiTest)
    debugImplementation(Deps.ComposeDeps.composeUiTooling)
    debugImplementation(ThirdDeps.leakCanary)
}