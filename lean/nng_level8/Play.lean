import NngLevel8

open MyNat

/-!
遊玩檔：Desktop Typewriter 只改 `by` 後面的 tactic 行。
不要在這裡留下 `sorry`；未完成時 Lean 會報 unsolved goals。
-/
theorem le_total (x y : ℕ) : x ≤ y ∨ y ≤ x := by
