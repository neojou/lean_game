/-
Adapted from NNG4 `Game/MyNat/Definition.lean`
https://github.com/leanprover-community/NNG4
Apache-2.0
-/

/-- Our copy of the natural numbers called `MyNat`, with notation `ℕ`. -/
inductive MyNat where
  | zero : MyNat
  | succ : MyNat → MyNat

attribute [pp_nodot] MyNat.succ

@[inherit_doc]
notation (name := MyNatNotation) (priority := 1000000) "ℕ" => MyNat

namespace MyNat

instance : Inhabited MyNat where
  default := MyNat.zero

def ofNat (x : Nat) : MyNat :=
  match x with
  | Nat.zero   => MyNat.zero
  | Nat.succ b => MyNat.succ (ofNat b)

def toNat (x : MyNat) : Nat :=
  match x with
  | MyNat.zero   => Nat.zero
  | MyNat.succ b => Nat.succ (toNat b)

instance instOfNat {n : Nat} : OfNat MyNat n where
  ofNat := ofNat n

instance : ToString MyNat where
  toString p := toString (toNat p)

theorem zero_eq_0 : MyNat.zero = 0 := rfl

def one : MyNat := MyNat.succ 0

/-- Custom recursor so the zero case pretty-prints as `0`, not `MyNat.zero`. -/
theorem rec' {P : ℕ → Prop} (zero : P 0)
    (succ : (n : ℕ) → (n_ih : P n) → P (succ n)) (t : ℕ) : P t := by
  induction t with
  | zero => exact zero
  | succ n ih => exact succ n ih

/-- Custom casesOn so the zero case pretty-prints as `0`. -/
def casesOn' {P : ℕ → Sort u} (t : ℕ) (zero : P 0)
    (succ : (a : ℕ) → P (MyNat.succ a)) : P t := by
  cases t with
  | zero => exact zero
  | succ n => exact succ n

end MyNat
