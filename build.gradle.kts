// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.com.android.application) apply false
    alias(libs.plugins.org.jetbrains.kotlin.android) apply false
    alias(libs.plugins.com.android.library) apply false
}

val androidMinSdkVersion by extra(28)
val androidTargetSdkVersion by extra(34)
val androidCompileSdkVersion by extra(34)

val androidSourceCompatibility by extra(JavaVersion.VERSION_11)
val androidTargetCompatibility by extra(JavaVersion.VERSION_11)
val androidKotlinJvmTarget by extra("11")
