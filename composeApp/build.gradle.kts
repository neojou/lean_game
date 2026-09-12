// KMP : commonMain + Desktop (wasm/JS not in current scope)

import java.io.File

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

group = "com.neojou.leangame"
version = "0.3.0"

val leanCommandDocsDir = rootProject.layout.projectDirectory.dir("docs/lean-commands")
val generatedLeanDocsDir = layout.buildDirectory.dir("generated/leanCommandDocs/kotlin")

val generateLeanCommandDocs by tasks.registering {
    description = "Embed docs/lean-commands/*.md into LeanCommandDocs at compile time."
    inputs.dir(leanCommandDocsDir)
    outputs.dir(generatedLeanDocsDir)
    doLast {
        val src = leanCommandDocsDir.asFile
        val outRoot = generatedLeanDocsDir.get().asFile
        val dest = File(outRoot, "com/neojou/leangame/docs/LeanCommandDocs.kt")
        dest.parentFile.mkdirs()
        val files = src.listFiles { _, name ->
            name.endsWith(".md") && !name.equals("README.md", ignoreCase = true)
        }?.sortedBy { it.name } ?: emptyList()
        fun escape(s: String): String =
            s.replace("\\", "\\\\")
                .replace("$", "\${'$'}")
                .replace("\"\"\"", "\"\"\${'\"'}\"")
        val entries = files.joinToString(",\n") { f ->
            val id = f.name.removeSuffix(".md")
            val body = escape(f.readText())
            "        \"$id\" to \"\"\"$body\"\"\""
        }
        dest.writeText(
            """
            |package com.neojou.leangame.docs
            |
            |/**
            | * Command help compiled from docs/lean-commands markdown files.
            | * Edit the Markdown and rebuild; runtime does not read those files.
            | */
            |object LeanCommandDocs {
            |    fun text(docId: String): String =
            |        texts[docId] ?: missing(docId)
            |
            |    fun has(docId: String): Boolean = texts.containsKey(docId)
            |
            |    val ids: Set<String> get() = texts.keys
            |
            |    private fun missing(docId: String): String =
            |        "（尚無「${'$'}docId」的說明。請在 docs/lean-commands/${'$'}docId.md 撰寫後重新編譯。）"
            |
            |    private val texts: Map<String, String> = mapOf(
            |$entries
            |    )
            |}
            |
            """.trimMargin()
        )
    }
}

kotlin {
    jvm("desktop")
    jvmToolchain(25)

    sourceSets {
        val commonMain by getting {
            kotlin.srcDir(generateLeanCommandDocs)
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
