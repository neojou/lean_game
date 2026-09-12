/-
Adapted from NNG4 `Game/MyNat/Addition.lean`
https://github.com/leanprover-community/NNG4
Apache-2.0
-/
import NngLevel8.Definition

namespace MyNat

opaque add : MyNat → MyNat → MyNat

instance instAdd : Add MyNat where
  add := MyNat.add

/-- `add_zero a` is a proof of `a + 0 = a`. -/
axiom add_zero (a : MyNat) : a + 0 = a

/-- `add_succ a d` is a proof of `a + succ d = succ (a + d)`. -/
axiom add_succ (a d : MyNat) : a + succ d = succ (a + d)

end MyNat
