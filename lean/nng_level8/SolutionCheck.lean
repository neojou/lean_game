import NngLevel8

open MyNat

-- Developer check for the official NNG4 solution. Not imported by Play.lean.
theorem le_total_check (x y : ℕ) : x ≤ y ∨ y ≤ x := by
  induction y with d hd
  right
  exact zero_le x
  cases hd with h1 h2
  left
  cases h1 with e h1
  rw [h1]
  use e + 1
  rw [succ_eq_add_one, add_assoc]
  rfl
  cases h2 with e he
  cases e with a
  rw [he]
  left
  rw [add_zero]
  use 1
  exact succ_eq_add_one d
  right
  use a
  rw [add_succ] at he
  rw [succ_add]
  exact he

-- `apply le_trans … at` is available at this level (NNG4 already unlocked le_trans).
theorem apply_le_trans_at_check (x d : ℕ) (hdl : x ≤ d) : x ≤ succ d := by
  apply le_trans x d (succ d) at hdl
  apply hdl
  exact le_succ_self d
