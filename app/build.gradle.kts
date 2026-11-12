// Cấu hình build cho ứng dụng UniMarket
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // dùng để sinh code cho Room
}

android {
    namespace = "com.unimarket"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.unimarket"
        minSdk = 29          // Android 10 trở lên
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true   // liên kết giao diện XML với Kotlin
    }
}

// Lưu cấu trúc database ra file JSON (tiện xem và bảo vệ đồ án)
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    // Thư viện giao diện cơ bản của Android
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.activity:activity-ktx:1.9.3")
    implementation("androidx.fragment:fragment-ktx:1.8.5")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    // Room: lưu dữ liệu bằng SQLite
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Kiểm thử thuật toán (chạy trên máy tính, không cần máy ảo)
    testImplementation("junit:junit:4.13.2")
}
