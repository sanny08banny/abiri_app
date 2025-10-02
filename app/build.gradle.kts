import java.util.Properties

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.androidApplication)
    kotlin("android") version "2.0.20"
    alias(libs.plugins.com.google.android.libraries.mapsplatform.secrets.gradle.plugin)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.sanny_tech.carapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.sanny_tech.carapp"
        minSdk = 24
        targetSdk = 35
        versionCode = 66
        versionName = "1.0"

        buildConfigField("String", "MAPS_API_KEY", properties["MAP_API_KEY"].toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.glide)
    implementation(libs.viewmodel)
    implementation(libs.gson)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.androidx.preference)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.play.services.maps)
    implementation(libs.android.maps.utils)
    implementation(libs.places)

    implementation(libs.androidx.activity)
    implementation(libs.firebase.analytics)
    implementation(libs.google.firebase.auth)
    implementation(libs.firebase.database)
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    implementation(libs.google.firebase.appcheck.playintegrity)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("org.java-websocket:Java-WebSocket:1.5.7")
    implementation(libs.androidx.palette)
    implementation(libs.play.services.location)
    implementation(libs.ccp)
    implementation("com.getkeepsafe.taptargetview:taptargetview:1.13.3")
    implementation(libs.androidx.credentials)

    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation("com.github.yukuku:ambilwarna:2.0.1")
    implementation(libs.integrity)

    implementation(libs.play.services.auth.v2120)
    implementation(libs.androidx.core.splashscreen)
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("com.google.android.play:app-update:2.1.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.facebook.shimmer:shimmer:0.5.0")
    implementation(project(":clientlib"))

//    implementation("com.github.sanny08banny:android-protocols-sdk:v1.0.6")
}