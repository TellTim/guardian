/**
 * To define dependencies
 */
object Deps {
    val appCompat by lazy { "androidx.appcompat:appcompat:${Versions.appCompat}" }
    val materialDesign by lazy { "com.google.android.material:material:${Versions.material}" }
    val constraintLayout by lazy { "androidx.constraintlayout:constraintlayout:${Versions.constraintLayout}" }
    val multidex by lazy { "androidx.multidex:multidex:${Versions.multidex}"}
    val annotation by lazy { "androidx.annotation:annotation:${Versions.annotation}" }


    object TestDeps {
        val junit by lazy { "junit:junit:${Versions.jUnit}" }
        val extJunit by lazy { "androidx.test.ext:junit:1.1.3" }
        val espresso by lazy { "androidx.test.espresso:espresso-core:3.4.0" }
    }


    object ComposeDeps {
        val composeUI by lazy { "androidx.compose.ui:ui:${Versions.composeVersion}" }
        val composeMaterial by lazy {
            "androidx.compose.material:material:${Versions.composeVersion}"
        }
        val composeUIPreview by lazy { "androidx.compose.ui:ui-tooling-preview:${Versions.composeVersion}"  }
        val activityCompose by lazy {
            "androidx.activity:activity-compose:${Versions.activityComposeVersion}"
        }

        val composeUiTest by lazy {
            "androidx.compose.ui:ui-test-junit4:${Versions.composeVersion}"
        }

        val composeUiTooling by lazy {
            "androidx.compose.ui:ui-tooling:${Versions.composeVersion}"
        }
    }

    object LifecycleDeps {
        val lifecycleRuntimeKtx by lazy{
            "androidx.lifecycle:lifecycle-runtime-ktx:2.4.1"
        }
    }

}

object KtDeps {
    val kotlin by lazy { "org.jetbrains.kotlin:kotlin-stdlib-jdk7:${Versions.kotlin}" }
    val ktx by lazy {"androidx.core:core-ktx:${Versions.coreKtx}"}
}

object ThirdDeps{
    val leakCanary by lazy { "com.squareup.leakcanary:leakcanary-android:${Versions.leakCanary}" }
}