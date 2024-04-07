plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
    id("androidx.navigation.safeargs.kotlin")
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
    packaging {
        resources.excludes.add("META-INF/*")
        resources.excludes.add("mozilla/*")
        resources.excludes.add("kotlin/*")
        resources.excludes.add("kotlin/internal/*")
        resources.excludes.add("kotlin/collections/*")
        resources.excludes.add("kotlin/annotation/*")
        resources.excludes.add("kotlin/reflect/*")
        resources.excludes.add("kotlin/coroutines/*")
        resources.excludes.add("xsd/*")
    }
}

dependencies {
    implementation(libs.androidx.activity)
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("com.google.android.material:material:1.11.0")
    implementation("com.google.firebase:firebase-auth:22.3.1")
    implementation("com.firebaseui:firebase-ui-auth:8.0.2")
    implementation(platform("com.google.firebase:firebase-bom:32.7.3"))
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-auth")
    //noinspection UseTomlInstead
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.navigation.safeargs:androidx.navigation.safeargs.gradle.plugin:2.7.7")
    //testImplementation(libs.junit)
    //androidTestImplementation(libs.androidx.junit)
    //androidTestImplementation(libs.androidx.espresso.core)

}