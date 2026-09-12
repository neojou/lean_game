package com.neojou.leangame.level

import com.neojou.leangame.proof.Goal

enum class InventoryKind {
    Tactic,
    Theorem,
    Definition,
}

/** NNG-style theorem groups shown as sub-tabs under 定理. */
enum class TheoremTab(val label: String) {
    Plus("+"),
    Times("*"),
    Pow("^"),
    Le("≤"),
    Numerals("012"),
    Peano("Peano"),
}

data class InventoryItem(
    val kind: InventoryKind,
    val name: String,
    val insertTemplate: String,
    val descriptionZh: String,
    val englishNote: String? = null,
    /** Filename stem under `docs/lean-commands/` (no `.md`). */
    val docId: String = name,
    val theoremTab: TheoremTab? = null,
)

data class HiddenHint(
    val id: String,
    val text: String,
    val matcher: HintMatcher,
)

sealed class HintMatcher {
    data object InitialTotalOrder : HintMatcher()
    data object InductiveOr : HintMatcher()
    data object RightLeHyp : HintMatcher()
    data object DiffNatCases : HintMatcher()
}

data class LevelContent(
    val worldName: String,
    val levelNumber: Int,
    val titleZh: String,
    val theoremName: String,
    val statementZh: String,
    val introduction: String,
    val conclusion: String,
    val hiddenHints: List<HiddenHint>,
    val tactics: List<InventoryItem>,
    val theorems: List<InventoryItem>,
    val definitions: List<InventoryItem>,
    val officialSolution: List<String>,
)

fun HintMatcher.matches(goals: List<Goal>): Boolean {
    if (goals.isEmpty()) return this is HintMatcher.InitialTotalOrder
    val allHyps = goals.flatMap { it.hyps }
    val targets = goals.joinToString("\n") { it.targetPretty }
    return when (this) {
        HintMatcher.InitialTotalOrder ->
            goals.any { initialTotalOrderTarget(it.targetPretty) } &&
                allHyps.none { "∨" in it.typePretty || "\\/" in it.typePretty }

        HintMatcher.InductiveOr ->
            allHyps.any { hyp ->
                val t = hyp.typePretty
                ("∨" in t || "\\/" in t) && ("≤" in t || "<=" in t)
            }

        HintMatcher.RightLeHyp ->
            allHyps.any { hyp ->
                val t = hyp.typePretty
                hyp.names.any { it == "h2" } &&
                    ("≤" in t || "<=" in t) &&
                    "∨" !in t && "\\/" !in t
            }

        HintMatcher.DiffNatCases -> {
            val hasE = allHyps.any { hyp ->
                hyp.names.any { it == "e" } &&
                    (hyp.typePretty.contains("ℕ") ||
                        hyp.typePretty.contains("MyNat") ||
                        hyp.typePretty.contains("Nat"))
            }
            val hasEq = allHyps.any { "=" in it.typePretty }
            val stillOr = "∨" in targets || "\\/" in targets
            hasE && hasEq && stillOr
        }
    }
}

private fun initialTotalOrderTarget(target: String): Boolean {
    val t = target.replace(" ", "")
    return ("x≤y∨y≤x" in t) || ("x<=y\\/y<=x" in t) || ("x≤y∨y≤x" in target.replace(" ", ""))
}

fun matchingHiddenHints(goals: List<Goal>, catalog: List<HiddenHint>): List<HiddenHint> =
    catalog.filter { it.matcher.matches(goals) }
