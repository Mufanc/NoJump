plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.hiddenapi.refine)
}

val androidMinSdkVersion: Int by rootProject.extra
val androidTargetSdkVersion: Int by rootProject.extra
val androidCompileSdkVersion: Int by rootProject.extra

val androidSourceCompatibility: JavaVersion by rootProject.extra
val androidTargetCompatibility: JavaVersion by rootProject.extra
val androidKotlinJvmTarget: String by rootProject.extra

android {
    namespace = "xyz.mufanc.nojump"
    compileSdk = androidCompileSdkVersion

    defaultConfig {
        applicationId = "xyz.mufanc.nojump"
        minSdk = androidMinSdkVersion
        targetSdk = androidTargetSdkVersion
        versionCode = 1
        versionName = "1.0"
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

    kotlinOptions {
        jvmTarget = androidKotlinJvmTarget
    }

    buildFeatures {
        buildConfig = true
        aidl = true
    }
}

dependencies {
    // shizuku
    implementation(libs.shizuku.api)
    implementation(libs.shizuku.provider)

    // hidden api
//    compileOnly(project(":api-stub"))
    compileOnly(libs.hiddenapi.rikka.stub)
    implementation(libs.hiddenapi.rikka.compat)
    implementation(libs.reflect.joor)

    // app
    implementation(libs.androidx.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
}
