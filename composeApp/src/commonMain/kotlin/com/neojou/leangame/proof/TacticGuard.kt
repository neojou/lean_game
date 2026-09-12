package com.neojou.leangame.proof

private val BANNED = Regex("""\b(sorry|admit|native_decide)\b""")

fun normalizeTacticLine(raw: String): String {
    var s = raw.trim()
    if (s.endsWith(',')) {
        s = s.dropLast(1).trimEnd()
    }
    return s
}

/**
 * Play-mode blacklist. Returns a Traditional Chinese reason, or null if allowed.
 */
fun tacticBlockedReason(raw: String): String? {
    val match = BANNED.find(raw) ?: return null
    return "遊玩模式不能使用 `${match.value}`。"
}

fun diagnosticIsUnsolvedGoals(message: String): Boolean {
    val m = message.lowercase()
    return "unsolved goals" in m || "unsolved goal" in m
}
