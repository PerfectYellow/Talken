import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("androidx.fragment:fragment-ktx:1.8.6")

            implementation(libs.ktor.client.android)
            implementation("androidx.security:security-crypto-ktx:1.1.0-alpha06")
            implementation("androidx.biometric:biometric-ktx:1.2.0-alpha05")
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
            implementation(libs.mlkit.barcode.scanning)
            implementation("org.bouncycastle:bcprov-jdk18on:1.79")
        }
        
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            implementation(libs.filekit.compose)
            implementation(libs.filekit.core)
            implementation(libs.krypto)
            implementation(libs.qrcode.kotlin)
            implementation("org.jetbrains.compose.material:material-icons-extended:1.7.3")

            implementation("org.jetbrains.kotlinx:kotlinx-collections-immutable:0.3.7")

            implementation("androidx.datastore:datastore-preferences:1.2.1")
            implementation("androidx.datastore:datastore:1.2.1")
            implementation("com.squareup.okio:okio:3.9.0")
            
            implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
            
            implementation("io.coil-kt.coil3:coil-compose:3.0.0-rc01")
            implementation("io.coil-kt.coil3:coil-network-ktor3:3.0.0-rc01")

            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)

            implementation(libs.koin.core)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.example.cyloop"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.cyloop"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}
