plugins {
    id ("com.android.application") version "8.3.1" apply false
    id("com.android.library") version "8.3.1" apply false
    id("androidx.navigation.safeargs.kotlin") version "2.7.7" apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
    }
}
