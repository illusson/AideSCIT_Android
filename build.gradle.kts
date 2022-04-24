// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val androidVer = "7.1.3"
    id("com.android.application") version androidVer apply false
    id("com.android.library") version androidVer apply false

    val kotlinVer = "1.6.20"
    id("org.jetbrains.kotlin.android") version kotlinVer apply false
//    id("org.jetbrains.kotlin.plugin.parcelize") version kotlinVer apply false
//    id("org.jetbrains.kotlin.kapt") version kotlinVer apply false
}

task("clean", Delete::class) {
    delete(rootProject.buildDir)
}