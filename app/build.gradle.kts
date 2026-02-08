import org.gradle.kotlin.dsl.main

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.kapt)
    alias (libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.ezycart"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.ezycart"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ndk {
            // Force the app to only use these architectures
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))
        }
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDirs(file("jniLibs"))
        }
       /* // If it still fails, explicitly add it to the flavors
        getByName("malaysia") {
            jniLibs.srcDirs("src/main/jniLibs")
        }
        getByName("saudi") {
            jniLibs.srcDirs("src/main/jniLibs")
        }*/
    }
    // Define flavor dimensions
    flavorDimensions += listOf("country")

    // Configure product flavors
    productFlavors {
        create("malaysia") {
            dimension = "country"
          //  applicationIdSuffix = ".my"
           // versionNameSuffix = "-my"
            // Set default values that can be overridden in build types
            buildConfigField("String", "BASE_URL", "\"https://uat-api-retailetics-ops-mini-03.retailetics.com\"")
            buildConfigField("String", "CURRENCY_SYMBOL", "\"RM\"")
            buildConfigField("String", "ACTIVATION_CODE", "\"ALpxvmI0111\"")
            buildConfigField( "String", "LOGS_BASE_URL", "\"https://uat-logs.retailetics.com\"")

        }

        create("saudi") {
            dimension = "country"
           // applicationIdSuffix = ".sa"
           // versionNameSuffix = "-sa"
            buildConfigField("String", "BASE_URL", "\"http://172.16.21.16:8086\"")
            buildConfigField("String", "CURRENCY_SYMBOL", "\"SAR\"")
            buildConfigField("String", "ACTIVATION_CODE", "\"ALpxvcc0022\"")
            buildConfigField( "String", "LOGS_BASE_URL", "\"https://uat-logs.retailetics.com\"")
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
           // applicationIdSuffix = ".debug"
          //  versionNameSuffix = "-debug"
            // You can override values for debug builds if needed
            buildConfigField("String", "BASE_URL", "\"https://uat-api-retailetics-ops-mini-03.retailetics.com\"")
           // buildConfigField("String", "BASE_URL", "\"https://api-tamimi-ezylite-ops01.retailetics.com\"")
           // buildConfigField("String", "BASE_URL", "\"http://172.16.21.16:8086\"")
            buildConfigField( "String", "LOGS_BASE_URL", "\"https://uat-logs.retailetics.com\"")
        }
        release {
            buildConfigField( "String", "LOGS_BASE_URL", "\"https://uat-logs.retailetics.com\"")
            buildConfigField("String", "BASE_URL", "\"https://api-retailetics-ops-mini-03.retailetics.com\"")
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // This will create variants like: malaysiaDebug, malaysiaRelease, saudiDebug, saudiRelease
    variantFilter {
        if (name == "malaysiaDebug" || name == "saudiDebug" ||
            name == "malaysiaRelease" || name == "saudiRelease") {
            ignore = false
        } else {
            ignore = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kapt {
        correctErrorTypes = true
    }
    buildFeatures {
        compose = true
        buildConfig = true // Enable build config generation
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.15"
    }
    packaging {
        jniLibs {
            useLegacyPackaging = true
            pickFirsts.add("**/*.so")
            //excludes += "**/libbxl_common.so.debug"// Forces traditional loading
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.material3.window.size)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Retrofit
    implementation(libs.retrofit2.retrofit)
    implementation(libs.retrofit2.converter.gson)
    implementation(libs.okhttp3.logging.interceptor)

    // Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // DataStore
    implementation(libs.androidx.datastore)

    // Zxing
    implementation(libs.zxing.core)
    implementation(libs.zxing.android.embedded)

    // Permission
    implementation(libs.permissions.compose)

    // Coil
    implementation(libs.coil.compose)

    // Lottie
    implementation(libs.lottie.compose)

    //WebView
    implementation(libs.accompanist.webview)

    //ML-Kit for Barcode scanner
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    implementation(libs.mlkit.barcode)

    // Custom Toast
    implementation(libs.dynamic.toasts)
    //Permission
    implementation(libs.accompanist.permissions)

    //Near Pay
    implementation(libs.nearpay.sdk.store)
    implementation(libs.work.runtime.ktx)
    implementation(libs.process.phoenix)

    //USB SERIAL COMMUNICATION
    implementation(libs.usb.serial.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))

    implementation(libs.jose4j)

    // Kotlin Serialization - Add constraints
    implementation(libs.kotlinx.serialization.json) {
        version {
            strictly("1.6.3")  // Force this version
        }
    }

    constraints {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json") {
            version {
                strictly("1.6.3")
            }
        }
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-core") {
            version {
                strictly("1.6.3")
            }
        }
    }
}

// Add resolution strategy
configurations.all {
    resolutionStrategy {
        eachDependency {
            when (requested.group) {
                "org.jetbrains.kotlinx" -> {
                    if (requested.name.startsWith("kotlinx-serialization-")) {
                        useVersion("1.6.3")
                        because("Force compatibility with Kotlin 1.9.25")
                    }
                }
            }
        }
        // Also force the BOM version
        force(
            "org.jetbrains.kotlinx:kotlinx-serialization-bom:1.6.3",
            "org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3",
            "org.jetbrains.kotlinx:kotlinx-serialization-core:1.6.3",
            "org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.6.3",
            "org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.6.3"
        )
    }
}