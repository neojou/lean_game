/-
Adapted from NNG4 `Game/MyNat/LE.lean`
https://github.com/leanprover-community/NNG4
Apache-2.0
-/
import NngLevel8.Addition

namespace MyNat

/-- `a ≤ b` means there exists `c` such that `b = a + c`. -/
def le (a b : ℕ) := ∃ (c : ℕ), b = a + c

instance : LE MyNat := ⟨MyNat.le⟩

end MyNat
