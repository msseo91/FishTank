plugins {
    id("com.android.library")
}

group = "com.github.philjay"

android {
    compileSdk = 33

    defaultConfig {
        minSdk = 14
        targetSdk = 33
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {
    implementation("androidx.annotation:annotation:1.5.0")
    testImplementation("junit:junit:4.13.2")
}


