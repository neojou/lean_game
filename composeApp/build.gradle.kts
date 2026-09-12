// KMP : commonMain + Desktop (wasm/JS not in current scope)

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.neojou.leangame"
version = "0.1.0"

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
            }
        }
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)
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
