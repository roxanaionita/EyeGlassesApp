plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    id("kotlin-kapt")
    id("com.google.devtools.ksp")
//    id("androidx.room")
}

android {
    namespace = "com.example.eyeglassesapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.eyeglassesapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Configure Room schema directory
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
//        kapt {
//            arguments {
//                arg("room.schemaLocation", "$projectDir/schemas")
//            }
//        }
//        ksp {
//            arg("room.schemaLocation", "$projectDir/schemas")
//        }

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
    buildFeatures{
        viewBinding = true
        dataBinding = true
    }
     //Configure Room schema directory
//    room {
//        schemaLocation = "$projectDir/schemas"
//    }
//
//    // Configure source sets for Kotlin files
    sourceSets {
        getByName("main").java.srcDirs("src/main/kotlin", "$projectDir/schemas")
    }
}


dependencies {
    implementation(libs.firebase.firestore.ktx)
    //implementation(libs.androidx.baseLibrary)

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    implementation("androidx.navigation:navigation-compose:2.7.7")

    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation ("com.caverock:androidsvg:1.4")
    implementation("com.airbnb.android:lottie:6.4.0")
    implementation ("com.google.android.material:material:1.11.0")

    implementation (platform("com.google.firebase:firebase-bom:32.8.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.android.gms:play-services-auth:21.0.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")


    //dots
    implementation ("androidx.viewpager2:viewpager2:1.0.0")
    implementation ("com.tbuonomo:dotsindicator:4.2")

    implementation("com.github.Philjay:MPAndroidChart:v3.1.0")

}