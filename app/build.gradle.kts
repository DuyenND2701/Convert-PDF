plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.example.covertpdfapplication"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.covertpdfapplication"
        minSdk = 31
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
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("androidx.gridlayout:gridlayout:1.0.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- 1. Nhóm Xương sống: Tạo và vẽ file PDF ---
    // iText 7 là thư viện mạnh nhất để tạo PDF từ nhiều nguồn khác nhau
    implementation("com.itextpdf:itext7-core:7.2.5")
    implementation("com.itextpdf:html2pdf:4.0.5") // Hỗ trợ nhóm Web (HTML)

    // --- 2. Nhóm Office: Document, Spreadsheet, Presentation ---
    // Xử lý: DOCX, XLSX, PPTX, DOC, XLS, PPT
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("com.github.and-home:poi-android:3.17-alpha") // Bản tối ưu cho Android

    // --- 3. Nhóm Image & SVG ---
    // Xử lý: JPG, PNG, WEBP và đặc biệt là SVG
    implementation("com.github.corouteam:GlideToVectorYou:2.0.4") // Hỗ trợ SVG
    implementation("com.github.bumptech.glide:glide:4.16.0") // Xử lý ảnh bitmap

    // --- 4. Nhóm Text & Markdown ---
    // Xử lý: MD, TXT, RTF
    implementation("org.commonmark:commonmark:0.21.0") // Parser cho Markdown

    // --- 5. Nhóm eBook ---
    // Xử lý: EPUB
    implementation("com.github.psiegman:epublib-core:4.0")

    // --- 6. Nhóm Email ---
    // Xử lý: EML, MSG
    implementation("com.sun.mail:android-mail:1.6.7")
    implementation("com.sun.mail:android-activation:1.6.7")

    // --- 7. Nhóm Archive (Giải nén trước khi convert) ---
    // Xử lý: ZIP, CBZ, CBR
    implementation("org.apache.commons:commons-compress:1.26.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}