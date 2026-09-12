// KMP : commonMain + Desktop (wasm/JS not in current scope)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.neojou.leangame"
version = "0.2.0"

kotlin {
    jvm("desktop")
    jvmToolchain(25)

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
            }
        }
    }
}

compose.desktop {
    application {
        mainClass = "com.neojou.leangame.MainKt"
    }
}

compose.resources {
    packageOfResClass = "com.neojou.leangame"
}
