package com.neojou.leangame

/**
 * Application product version — **single source of truth** for UI / About.
 *
 * When bumping a release, update these constants first, then follow
 * [docs/VERSIONING.md](../../../../../docs/VERSIONING.md) (repo root).
 *
 * Scheme (product-facing, not forced SemVer):
 * - [NAME]: `MAJOR.MINOR` (e.g. `"0.3"`) or `MAJOR.MINOR.PATCH` when needed
 * - [DISPLAY]: shown in About, typically `"v" + NAME`
 *
 * 版本紀錄見 repo 根目錄 [CHANGELOG.md](../../../../../CHANGELOG.md)。
 */
object AppVersion {
    /** Product name (Traditional Chinese). */
    const val APP_NAME: String = "LEAN Game"

    /** English / package short name. */
    const val APP_NAME_EN: String = "LEAN Game"

    /**
     * Marketing / product version string (no leading `v`).
     * Current release: **0.3**
     */
    const val NAME: String = "0.3"

    /** User-visible label, e.g. `v0.3`. */
    const val DISPLAY: String = "v$NAME"

    /** One-line blurb for About. */
    const val SUMMARY: String = "第八關 Typewriter＋指令說明"
}
