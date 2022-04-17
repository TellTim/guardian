/*buildscript {
    val compose_version by extra("1.0.1")
}*/
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "7.1.2" apply false
    id("com.android.library") version "7.1.2" apply false
    id("org.jetbrains.kotlin.android") version "1.6.20" apply false
    id("org.jetbrains.kotlin.jvm") version Versions.kotlin apply false
}

tasks.register("clean", Delete::class){
    delete(rootProject.buildDir)
}