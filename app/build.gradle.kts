plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-kapt")
}

android {
    namespace = "com.example.grocify"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.grocify"
        minSdk = 31
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "CLIENT_ID", "\"utgrocify-3f040506b20fcb9697595d8cd3788acc8979438697547936464\"")
        buildConfigField("String", "CLIENT_SECRET", "\"aZT99ZlTwIgUSFa4Mhqx3rsdlbJg_EEDAdp4zu0y\"")
        buildConfigField("String", "PRODUCT_SCOPE", "\"product.compact\"")
        buildConfigField("String", "CART_SCOPE", "\"cart.basic:write\"")

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
        //noinspection DataBindingWithoutKapt
        dataBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.converter.gson)
    implementation(libs.firebase.auth)
    implementation(platform(libs.firebase.bom.v3273))
    implementation(libs.firebase.ui.auth)
    implementation(libs.firebase.ui.storage)
    implementation(libs.glide)
    implementation(libs.imageSlideshow)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logging.interceptor)
    implementation(libs.material)
    implementation(libs.retrofit)

    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-analytics")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-analytics-ktx")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-appcheck-playintegrity")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-auth")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-auth-ktx")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-firestore")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-firestore-ktx")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-storage")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-storage-ktx")
    //noinspection KaptUsageInsteadOfKsp
    kapt(libs.compiler)

    annotationProcessor(libs.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}