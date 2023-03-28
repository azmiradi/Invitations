import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
}

group = "com.example.invitations"
version = "1.0-SNAPSHOT"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("com.google.firebase:firebase-admin:9.1.1")
                implementation ("com.google.zxing:core:3.4.1")
                implementation ("com.google.zxing:javase:3.4.1")
                //implementation ("org.slf4j:slf4j-api:1.7.30")
                //implementation ("org.slf4j:slf4j-simple:1.7.30")
                implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0-native-mt")

            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "Invitations"
            packageVersion = "1.0.0"
        }
    }
}
