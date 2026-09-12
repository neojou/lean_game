/-
Theorems unlocked before NNG4 LessOrEqual level 8.
Provided as axioms so this playground can host `le_total` alone.
Statements match NNG4; we do not re-invent the mathematics.
-/
import NngLevel8.LE

namespace MyNat

/-- `zero_le x` : `0 ≤ x`. -/
axiom zero_le (x : ℕ) : 0 ≤ x

/-- `succ_add a b` : `succ a + b = succ (a + b)`. -/
axiom succ_add (a b : ℕ) : succ a + b = succ (a + b)

/-- `add_assoc a b c` : `(a + b) + c = a + (b + c)`. -/
axiom add_assoc (a b c : ℕ) : a + b + c = a + (b + c)

/-- `succ_eq_add_one n` : `succ n = n + 1`. -/
axiom succ_eq_add_one (n : ℕ) : succ n = n + 1

/--
`le_trans x y z` : 若 `x ≤ y` 且 `y ≤ z`，則 `x ≤ z`。
（NNG4 LessOrEqual 第 4 關已解鎖。）
-/
axiom le_trans (x y z : ℕ) : x ≤ y → y ≤ z → x ≤ z

/--
`le_succ_self x` : `x ≤ succ x`。
（NNG4 LessOrEqual 第 3 關已解鎖。）
-/
axiom le_succ_self (x : ℕ) : x ≤ succ x

end MyNat
