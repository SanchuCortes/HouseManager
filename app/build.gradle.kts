plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.housemanager"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.housemanager"
        minSdk = 21
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // Lee valores de gradle.properties (o de variables de entorno si las prefieres)
        val apiKey = (project.findProperty("FOOTBALL_API_KEY") as String?)
            ?: System.getenv("FOOTBALL_API_KEY")
            ?: ""
        val apiBaseUrl = (project.findProperty("FOOTBALL_API_BASE_URL") as String?)
            ?: System.getenv("FOOTBALL_API_BASE_URL")
            ?: ""

        // Inyecta en BuildConfig
        buildConfigField("String", "FOOTBALL_API_KEY", "\"$apiKey\"")
        buildConfigField("String", "FOOTBALL_API_BASE_URL", "\"$apiBaseUrl\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Export schema de Room a app/schemas
        javaCompileOptions {
            annotationProcessorOptions {
                arguments(
                    mapOf(
                        "room.schemaLocation" to file("$projectDir/schemas").path,
                        "room.incremental" to "true",
                        "room.expandProjection" to "true"
                    )
                )
            }
        }
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true   // <- requerido para que se genere BuildConfig
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.google.android.material:material:1.11.0")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
