plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "vip.smart3makerspaces.peoplecounter"
    compileSdk = 34

    defaultConfig {
        applicationId = "vip.smart3makerspaces.peoplecounter"
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

        // Support for Java 8 features
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Add support for Kotlin coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")

    // CameraX dependencies
    val cameraxVersion = "1.3.0"
    implementation("androidx.camera:camera-core:$cameraxVersion")
    implementation("androidx.camera:camera-camera2:$cameraxVersion")
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion")
    implementation("androidx.camera:camera-view:$cameraxVersion")
    implementation("androidx.camera:camera-extensions:$cameraxVersion")

    // Required for Jetpack photo picker
    implementation("androidx.activity:activity-ktx:1.8.0")

    // AWS Amplify dependencies
    implementation("com.amplifyframework:core:2.14.1")
    implementation("com.amplifyframework:core-kotlin:2.14.1")
    implementation("com.amplifyframework:aws-predictions:2.14.1")
    implementation("com.amplifyframework:aws-auth-cognito:2.14.1")

    // Image compression library
    implementation("id.zelory:compressor:3.0.1")

    // Chart and graphing library
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Room database dependencies
    val roomVersion = "2.6.0"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion")

    // Add support for LiveData
    val lifecycleVersion = "2.6.2"
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:$lifecycleVersion")
    ksp("androidx.lifecycle:lifecycle-compiler:$lifecycleVersion")

    // Add support for Java 8 features
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")

    // Add support for Google Sheets API
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.api-client:google-api-client-android:2.2.0") {
        exclude(group = "org.apache.httpcomponents")
    }
    implementation("com.google.apis:google-api-services-sheets:v4-rev612-1.25.0") {
        exclude(group = "org.apache.httpcomponents")
    }
}