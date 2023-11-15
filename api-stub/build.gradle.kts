plugins {
    alias(libs.plugins.com.android.library)
}

val androidMinSdkVersion: Int by rootProject.extra
val androidCompileSdkVersion: Int by rootProject.extra

val androidSourceCompatibility: JavaVersion by rootProject.extra
val androidTargetCompatibility: JavaVersion by rootProject.extra

android {
    namespace = "hidden.api.stub"
    compileSdk = androidCompileSdkVersion

    defaultConfig {
        minSdk = androidMinSdkVersion
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles("proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = androidSourceCompatibility
        targetCompatibility = androidTargetCompatibility
    }
}

dependencies {
    compileOnly(libs.hiddenapi)
    annotationProcessor(libs.hiddenapi.processor)
}
