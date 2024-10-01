plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")

    kotlin("kapt")
    id("kotlin-android")
    id ("realm-android")
}

android {
    namespace = "com.hardik.notepad"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hardik.notepad"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.8.4")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.4")
    implementation ("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation ("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation ("androidx.legacy:legacy-support-v4:1.0.0")
    implementation ("androidx.activity:activity-ktx:1.9.0")
    implementation ("androidx.fragment:fragment-ktx:1.8.1")
    // Testing Fragments in Isolation
    debugImplementation ("androidx.fragment:fragment-testing:1.8.1")

    //Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.49")
    ksp("com.google.dagger:hilt-compiler:2.49")

    // Room
    implementation ("androidx.room:room-runtime:2.6.1")
    ksp ("androidx.room:room-compiler:2.6.1")

    // Realm
    implementation ("io.realm:realm-android-library:10.15.1") // Check for the latest version

    // Kotlin Extensions and Coroutines support for Room
    implementation ("androidx.room:room-ktx:2.6.1")

    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("com.opencsv:opencsv:5.7.1")

    // Coroutines
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")

//    implementation "io.coil-kt:coil:1.2.1"

    implementation ("com.intuit.sdp:sdp-android:1.1.0")
    implementation ("com.intuit.ssp:ssp-android:1.1.0")

    // Gradle Groovy DSL
//    implementation 'org.apache.commons:commons-csv:1.5' // for Kotlin JS platform
//    implementation 'com.opencsv:opencsv:4.6' // Here is opencsv library

    // Test dependencies
//    testImplementation ("junit:junit:4.13.2")
    testImplementation ("org.mockito:mockito-core:5.12.0")
    testImplementation ("org.mockito:mockito-inline:5.2.0")
    testImplementation ("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

}