// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version Versions.application apply false
    id("com.android.library") version Versions.library apply false
    id("org.jetbrains.kotlin.android") version Versions.kotlin apply false
    id("org.jetbrains.kotlin.jvm") version Versions.kotlin apply false
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}