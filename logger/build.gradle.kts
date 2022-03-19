plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    compileSdk = AppConfigs.compileSdkVersion

    defaultConfig {
        minSdk = AppConfigs.minSdkVersion
        targetSdk = AppConfigs.targetSdkVersion
    }

    lint {
        textReport = true
        textOutput = file("stdout")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    testOptions{
        unitTests { isReturnDefaultValues=true }
    }
}

dependencies {
    implementation(Deps.annotation)
}

