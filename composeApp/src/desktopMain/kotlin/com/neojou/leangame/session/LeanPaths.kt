package com.neojou.leangame.session

import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.isExecutable

object LeanPaths {
    const val EXPECTED_TOOLCHAIN: String = "leanprover/lean4:v4.33.1"

    fun extraBinDirs(): List<Path> {
        val home = System.getProperty("user.home")
        return listOf(
            Path.of(home, ".elan", "bin"),
            Path.of("/opt/homebrew/bin"),
            Path.of("/usr/local/bin"),
        )
    }

    fun augmentedPath(): String {
        val current = System.getenv("PATH") ?: ""
        val extras = extraBinDirs().map { it.toString() }
        return (extras + current.split(File.pathSeparator)).distinct().joinToString(File.pathSeparator)
    }

    fun findExecutable(name: String): Path? {
        val dirs = extraBinDirs() +
            (System.getenv("PATH") ?: "").split(File.pathSeparator).map { Path.of(it) }
        for (dir in dirs) {
            val candidate = dir.resolve(name)
            if (candidate.exists() && candidate.isExecutable()) return candidate
        }
        return null
    }

    fun findNngProject(): Path? {
        val starts = buildList {
            System.getProperty("user.dir")?.let { add(Path.of(it).toAbsolutePath()) }
            add(Path.of("").toAbsolutePath())
        }
        for (start in starts.distinct()) {
            var dir: Path? = start
            repeat(12) {
                val current = dir ?: return@repeat
                val marker = current.resolve("lean/nng_level8/lakefile.toml")
                if (Files.exists(marker)) return marker.parent
                dir = current.parent
            }
        }
        return null
    }

    fun readToolchain(project: Path): String? {
        val file = project.resolve("lean-toolchain")
        if (!Files.exists(file)) return null
        return Files.readString(file).lineSequence().firstOrNull { it.isNotBlank() }?.trim()
    }

    fun runVersion(lean: Path): String? = runCatching {
        val pb = ProcessBuilder(lean.toString(), "--version")
        pb.environment()["PATH"] = augmentedPath()
        pb.redirectErrorStream(true)
        val proc = pb.start()
        val text = proc.inputStream.readBytes().toString(Charsets.UTF_8).trim()
        proc.waitFor()
        text.lineSequence().firstOrNull()
    }.getOrNull()
}
