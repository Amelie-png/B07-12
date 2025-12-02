plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.demoapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.demoapp"
        minSdk = 24
        targetSdk = 36
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
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {

    // -------------------------------------
    // Android / UI dependencies
    // -------------------------------------
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.1.5")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.recyclerview)
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")


    // Navigation
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")

    // Chips
    implementation("com.google.android.material:material:1.12.0")

    // GraphView library
    implementation("com.jjoe64:graphview:4.2.2") {
        exclude(group = "com.android.support", module = "support-compat")
    }

    // -------------------------------------
    // Firebase
    // -------------------------------------
    implementation(platform("com.google.firebase:firebase-bom:34.5.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")

    // -------------------------------------
    // Unit Tests (These are what you need!)
    // -------------------------------------
    testImplementation("junit:junit:4.13.2")

    // Mockito Core
    testImplementation("org.mockito:mockito-core:5.7.0")

    // Required to mock Firebase final classes (Task, AuthResult, DocumentSnapshot)
    testImplementation("org.mockito:mockito-inline:5.2.0")

    // Mockito Android (helps with Firebase Task behavior)
    testImplementation("org.mockito:mockito-android:5.2.0")

    // Hamcrest matchers
    testImplementation("org.hamcrest:hamcrest-all:1.3")

    // -------------------------------------------------
    // Android Instrumented Tests
    // -------------------------------------------------
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
