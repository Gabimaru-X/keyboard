import java.io.File

plugins {
    id("com.android.application")
}

val keystoreFile: File? = System.getenv("KEYSTORE_FILE")?.let { File(it) }
val keystorePass: String? = System.getenv("KEYSTORE_PASS")
val signingKeyPass: String? = System.getenv("KEY_PASS")?.takeIf { it.isNotEmpty() } ?: keystorePass
val signingAlias: String = System.getenv("KEYSTORE_ALIAS")?.takeIf { it.isNotEmpty() } ?: "revamped"

fun shared(key: String): String = rootProject.file("placeholder.txt").readLines()
    .firstOrNull { it.trim().startsWith(key, ignoreCase = true) }
    ?.substringAfter(":")?.trim()
    ?.takeIf { it.isNotEmpty() }
    ?: "1.0.0"

val appVersion = shared("Appversion")
val appVersionCode = shared("Versioncode").toIntOrNull() ?: 1
val canSign = keystoreFile != null && keystoreFile.length() > 0 && !keystorePass.isNullOrEmpty()

android {
    namespace = "gnosis.keyboard.revamped"
    compileSdk = 37

    defaultConfig {
        applicationId = "gnosis.keyboard.revamped"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersion
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    signingConfigs {
        if (canSign) {
            create("release") {
                storeFile = keystoreFile
                storePassword = keystorePass
                keyAlias = signingAlias
                keyPassword = signingKeyPass
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (canSign) {
                signingConfigs.findByName("release")?.let { signingConfig = it }
            } else {
                signingConfig = signingConfigs.getByName("debug")
            }
        }
    }

    androidResources {
        noCompress += listOf("ttf", "flac")
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-ktx:1.13.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20231013")
}
