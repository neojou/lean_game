/-
Pedagogical tactic syntax matching NNG4 Typewriter for LessOrEqual level 8:
`induction n with d hd`, `cases h with a ha`, `use`, `left`/`right`,
and `rw` without auto-`rfl`.

Adapted from NNG4 `Game/Tactic/{Induction,Cases,Use,LeftRight,Rw}.lean`
(Apache-2.0), reimplemented against Lean 4 core — no Mathlib, no GameServer.
-/
import NngLevel8.LE
import Lean.Elab.Tactic.Basic
import Lean.Elab.Tactic.ElabTerm
import Lean.Meta.Tactic.Replace
import Lean.Parser.Tactic

namespace NngLevel8.Tactics
open Lean Meta Elab Tactic Parser.Tactic

private def asIdent (n : Name) : Ident := mkIdent n

/-- NNG-style `cases h with a ha` / `cases e with a`. -/
elab (name := nngCases) "cases " h:ident " with" ids:(ppSpace colGt ident)+ : tactic => do
  withMainContext do
    let names : Array Name := ids.map (·.getId)
    let hId := h.getId
    let some decl := (← getLCtx).findFromUserName? hId
      | throwError "找不到假設 {hId}"
    let type ← whnf (← inferType (mkFVar decl.fvarId))
    match type.getAppFn.constName? with
    | some ``Or =>
      let h1 := asIdent (names[0]?.getD `h1)
      let h2 := asIdent (names[1]?.getD `h2)
      evalTactic (← `(tactic|
        cases $(mkIdent hId):ident with
        | inl $h1 => ?inl
        | inr $h2 => ?inr))
    | some ``Exists =>
      let w := asIdent (names[0]?.getD `w)
      let p := asIdent (names[1]?.getD `h)
      evalTactic (← `(tactic|
        cases $(mkIdent hId):ident with
        | intro $w $p => ?ex))
    | some ``MyNat =>
      let a := asIdent (names[0]?.getD `a)
      evalTactic (← `(tactic|
        cases $(mkIdent hId):ident using MyNat.casesOn' with
        | zero => ?z
        | succ $a => ?s))
    | some ``LE.le | some ``MyNat.le =>
      let w := asIdent (names[0]?.getD `w)
      let p := asIdent (names[1]?.getD `h)
      evalTactic (← `(tactic|
        cases (show ∃ c, _ from $(mkIdent hId):ident) with
        | intro $w $p => ?ex))
    | other =>
      throwError "cases with：尚未支援這個型{indentD type} ({other})"

/-- NNG-style `induction y with d hd`. -/
elab (name := nngInduction) "induction " n:ident " with" d:ident hd:ident : tactic => do
  let tgt ← `(elimTarget| $n:ident)
  evalTactic (← `(tactic|
    induction $tgt using MyNat.rec' with
    | zero => ?z
    | succ $d $hd => ?s))

/-- `use t` for `∃` goals (no auto-rfl). -/
elab (name := nngUse) "use " t:term : tactic => do
  evalTactic (← `(tactic| refine ⟨$t, ?_⟩))

/-- `left` : prove the left side of `P ∨ Q`. -/
elab (name := nngLeft) "left" : tactic => do
  evalTactic (← `(tactic| refine Or.inl ?_))

/-- `right` : prove the right side of `P ∨ Q`. -/
elab (name := nngRight) "right" : tactic => do
  evalTactic (← `(tactic| refine Or.inr ?_))

/-- NNG-style `apply t at h`：把定理用在假設上（forward reasoning）。
例如 `apply le_trans x d (succ d) at hdl`。 -/
elab (name := nngApplyAt) "apply " t:term " at " h:ident : tactic => do
  withMainContext do
    let some ldecl := (← getLCtx).findFromUserName? h.getId
      | throwError "找不到假設 {h.getId}"
    let mut f ← elabTerm t none
    let mut extra : Array MVarId := #[]
    let mut matched := false
    for _ in *...16 do
      let ty ← whnf (← inferType f)
      match ty with
      | .forallE _ d _ _ =>
        if ← isDefEq d ldecl.type then
          f := mkApp f ldecl.toExpr
          matched := true
          break
        else
          let m ← mkFreshExprMVar d
          extra := extra.push m.mvarId!
          f := mkApp f m
      | _ => break
    unless matched do
      throwError "apply at：這個項的前提對不上假設 {h.getId}"
    let extraGoals ← extra.filterM fun id => return !(← id.isAssigned)
    let result ← (← getMainGoal).replace ldecl.fvarId f
    replaceMainGoal (result.mvarId :: extraGoals.toList)

/-- `rw` like NNG: rewrite without trailing `rfl`. Overrides the core macro. -/
macro (priority := high) "rw " c:optConfig s:rwRuleSeq l:(location)? : tactic =>
  `(tactic| rewrite $c $s:rwRuleSeq $[$l:location]?)

end NngLevel8.Tactics
