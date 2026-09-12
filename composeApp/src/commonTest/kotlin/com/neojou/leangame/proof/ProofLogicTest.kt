package com.neojou.leangame.proof

import com.neojou.leangame.level.Level8
import com.neojou.leangame.level.matchingHiddenHints
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProofLogicTest {
    @Test
    fun inventoryIncludesLeTransAndApply() {
        assertTrue(Level8.theorems.any { it.name == "le_trans" })
        assertTrue(Level8.theorems.any { it.name == "le_succ_self" })
        assertTrue(Level8.tactics.any { it.name == "apply" })
    }

    @Test
    fun normalizeDropsTrailingComma() {
        assertEquals("induction y with d hd", normalizeTacticLine("  induction y with d hd,  "))
    }

    @Test
    fun blacklistSorryAdmitNativeDecide() {
        assertNotNull(tacticBlockedReason("sorry"))
        assertNotNull(tacticBlockedReason("exact sorry"))
        assertNotNull(tacticBlockedReason("admit"))
        assertNotNull(tacticBlockedReason("native_decide"))
        assertNull(tacticBlockedReason("induction y with d hd"))
    }

    @Test
    fun unsolvedGoalsAreIncompleteNotFailure() {
        assertTrue(diagnosticIsUnsolvedGoals("unsolved goals"))
        assertFalse(diagnosticIsUnsolvedGoals("unknown identifier 'nope'"))
    }

    @Test
    fun playFileCursorAtByWhenEmpty() {
        val (line, col) = PlayFile.cursorAtEnd(emptyList())
        val rendered = PlayFile.render(emptyList())
        val lines = rendered.lines().filter { it.isNotBlank() }
        assertTrue(lines.last().endsWith(":= by"))
        assertEquals(lines.last().length, col)
        assertTrue(line >= 0)
    }

    @Test
    fun playFileIndentsTactics() {
        val text = PlayFile.render(listOf("induction y with d hd", "right"))
        assertTrue(text.contains("  induction y with d hd"))
        assertTrue(text.contains("  right"))
        assertFalse("\n  sorry" in text)
    }

    @Test
    fun parsePlainGoalHypsAndTarget() {
        val goal = parseOneGoal(
            """
            case succ
            x d : ℕ
            hd : x ≤ d ∨ d ≤ x
            ⊢ x ≤ succ d ∨ succ d ≤ x
            """.trimIndent(),
        )
        assertEquals("succ", goal.userName)
        assertEquals(listOf("x", "d"), goal.hyps[0].names)
        assertEquals("ℕ", goal.hyps[0].typePretty)
        assertEquals(listOf("hd"), goal.hyps[1].names)
        assertEquals("x ≤ succ d ∨ succ d ≤ x", goal.targetPretty)
    }

    @Test
    fun hiddenHintsMatchOfficialNodes() {
        val start = listOf(
            Goal(hyps = listOf(Hypothesis(listOf("x", "y"), "ℕ")), targetPretty = "x ≤ y ∨ y ≤ x"),
        )
        assertEquals(listOf("h1"), matchingHiddenHints(start, Level8.hiddenHints).map { it.id })

        val ih = listOf(
            Goal(
                hyps = listOf(
                    Hypothesis(listOf("x", "d"), "ℕ"),
                    Hypothesis(listOf("hd"), "x ≤ d ∨ d ≤ x"),
                ),
                targetPretty = "x ≤ succ d ∨ succ d ≤ x",
            ),
        )
        assertEquals(listOf("h2"), matchingHiddenHints(ih, Level8.hiddenHints).map { it.id })

        val right = listOf(
            Goal(
                hyps = listOf(
                    Hypothesis(listOf("x", "d"), "ℕ"),
                    Hypothesis(listOf("h2"), "d ≤ x"),
                ),
                targetPretty = "x ≤ succ d ∨ succ d ≤ x",
            ),
        )
        assertEquals(listOf("h3"), matchingHiddenHints(right, Level8.hiddenHints).map { it.id })

        val diff = listOf(
            Goal(
                hyps = listOf(
                    Hypothesis(listOf("x", "d"), "ℕ"),
                    Hypothesis(listOf("e"), "ℕ"),
                    Hypothesis(listOf("he"), "x = d + e"),
                ),
                targetPretty = "x ≤ succ d ∨ succ d ≤ x",
            ),
        )
        assertEquals(listOf("h4"), matchingHiddenHints(diff, Level8.hiddenHints).map { it.id })
    }
}
