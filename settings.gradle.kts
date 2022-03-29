pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Guardian"
include(":app",":ble",":auth",
    ":device",":net",":db",
    ":common",":audioplayer",":videoplayer",
    ":upgrade",":data-figure",":help",
    ":voice",":camera",":common-java",
    ":startup",":xcrash",":logger",
    ":xthread-task"
)
