package com.neojou.leangame.proof

/**
 * Builds the Play.lean buffer the language server sees.
 * Only the `by` block is rewritten; the statement stays fixed.
 */
object PlayFile {
    const val FILE_NAME: String = "Play.lean"

    const val HEADER: String =
        """import NngLevel8

open MyNat

/-!
遊玩檔：Desktop Typewriter 只改 `by` 後面的 tactic 行。
不要在這裡留下 `sorry`；未完成時 Lean 會報 unsolved goals。
-/
theorem le_total (x y : ℕ) : x ≤ y ∨ y ≤ x := by"""

    fun render(tactics: List<String>): String {
        if (tactics.isEmpty()) return HEADER.trimEnd() + "\n"
        val body = tactics.joinToString("\n") { "  $it" }
        return HEADER.trimEnd() + "\n" + body + "\n"
    }

    /**
     * LSP position (0-based line / UTF-16 character) at the end of the last
     * tactic line, or at `by` when no tactics have been sent.
     */
    fun cursorAtEnd(tactics: List<String>): Pair<Int, Int> {
        val lines = render(tactics).lines()
        val idx = lines.indexOfLast { it.isNotBlank() }.coerceAtLeast(0)
        return idx to lines[idx].length
    }
}
