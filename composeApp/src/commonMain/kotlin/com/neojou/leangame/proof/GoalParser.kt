package com.neojou.leangame.proof

/**
 * Parse `$/lean/plainGoal` pretty-printed goal blocks into hyps + target.
 */
fun parsePlainGoals(goalBlocks: List<String>): List<Goal> =
    goalBlocks.map { parseOneGoal(it) }.filter {
        it.targetPretty.isNotBlank() || it.hyps.isNotEmpty()
    }

fun parseOneGoal(text: String): Goal {
    val lines = text.replace("\r\n", "\n").trim().lines()
    var userName: String? = null
    val hyps = mutableListOf<Hypothesis>()
    var target = ""
    for (raw in lines) {
        val line = raw.trim()
        when {
            line.startsWith("case ") ->
                userName = line.removePrefix("case ").trim()
            line.startsWith("⊢") || line.startsWith("|-") ->
                target = line.removePrefix("⊢").removePrefix("|-").trim()
            " : " in line -> {
                val idx = line.indexOf(" : ")
                val names = line.substring(0, idx)
                    .split(Regex("""\s+"""))
                    .filter { it.isNotEmpty() }
                val type = line.substring(idx + 3).trim()
                if (names.isNotEmpty()) {
                    hyps += Hypothesis(names, type)
                }
            }
        }
    }
    return Goal(hyps = hyps, targetPretty = target, userName = userName)
}
