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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.fragment)
    implementation(libs.itext.kernel)
    implementation(libs.itext.layout)
    implementation(libs.itext.io)
    implementation(libs.poi) {
        exclude(group = "org.bouncycastle")
    }
    implementation(libs.poi.ooxml) {
        exclude(group = "org.bouncycastle")
        exclude(group = "xml-apis", module = "xml-apis")
    }
    implementation(libs.poi.scratchpad) {
        exclude(group = "org.bouncycastle")
    }
    implementation(libs.bouncy.bcprov)
    implementation(libs.bouncy.bcpkix)
    implementation("com.google.code.gson:gson:2.13.2")
    implementation(libs.pdfbox.android)
    implementation(libs.commons.csv)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}