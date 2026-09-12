package com.neojou.leangame.level

/**
 * NNG4 LessOrEqual level 8 — copy from AGENTS.md §7–§8.
 * Tactic / theorem names stay English; prose is Traditional Chinese.
 */
val Level8: LevelContent = LevelContent(
    worldName = "≤ 世界",
    levelNumber = 8,
    titleZh = "x ≤ y 或 y ≤ x",
    theoremName = "le_total",
    statementZh = "若 x 與 y 是自然數，則 x ≤ y 或 y ≤ x。",
    introduction = """
這大概是目前最硬的一關。提示：若 `a` 是一個數，`cases a with b` 會拆成 `a = 0` 與 `a = succ b` 兩種情形。
在假設還沒保證你能證出結果之前，先別急著 `left` 或 `right`。

我留了隱藏提示；若需要，從頭再試一次，並點「顯示更多提示！」。
    """.trimIndent(),
    conclusion = "非常好。路過的數學家說：你剛剛證明了 `ℕ` 是全序的。這個世界剩下的關卡輕鬆多了。",
    hiddenHints = listOf(
        HiddenHint(
            id = "h1",
            text = "開頭：先做 `induction y with d hd`。",
            matcher = HintMatcher.InitialTotalOrder,
        ),
        HiddenHint(
            id = "h2",
            text = "歸納步驟、面前是 `hd : x ≤ d ∨ d ≤ x`：試 `cases hd with h1 h2`。",
            matcher = HintMatcher.InductiveOr,
        ),
        HiddenHint(
            id = "h3",
            text = "右支、`h2 : d ≤ x`：接著 `cases h2 with e he`。",
            matcher = HintMatcher.RightLeHyp,
        ),
        HiddenHint(
            id = "h4",
            text = "仍不知道該往左還是往右：對差做 `cases e with a`。",
            matcher = HintMatcher.DiffNatCases,
        ),
    ),
    tactics = listOf(
        InventoryItem(
            InventoryKind.Tactic, "rfl", "rfl",
            "當目標兩邊**看起來完全一樣**時收掉等式。",
        ),
        InventoryItem(
            InventoryKind.Tactic, "rw", "rw []",
            "`h : X = Y` 時，`rw [h]` 把目標裡的 `X` 改成 `Y`。反向用 `rw [← h]`；改假設用 `rw [h] at h2`。",
        ),
        InventoryItem(
            InventoryKind.Tactic, "exact", "exact ",
            "若手上的項／定理剛好就是目標，直接交出證明。",
        ),
        InventoryItem(
            InventoryKind.Tactic, "induction", "induction y with d hd",
            "對自然數歸納。零情形與 `succ d` 情形分開；`hd` 是歸納假設。",
        ),
        InventoryItem(
            InventoryKind.Tactic, "cases", "cases h with a ha",
            "依類型拆假設或變數。`ℕ` 拆成 `0` / `succ`；`∨` 拆成左右支；`∃` 拆出見證。",
        ),
        InventoryItem(
            InventoryKind.Tactic, "left", "left",
            "目標是 `P ∨ Q` 時，改證 `P`。沒把握就別先按。",
        ),
        InventoryItem(
            InventoryKind.Tactic, "right", "right",
            "目標是 `P ∨ Q` 時，改證 `Q`。",
        ),
        InventoryItem(
            InventoryKind.Tactic, "use", "use ",
            "目標是 `∃ c, ...` 時，提出見證。本遊戲裡 `a ≤ b` 就是 `∃ c, b = a + c`。",
        ),
        InventoryItem(
            InventoryKind.Tactic, "apply", "apply  at ",
            "把定理用在假設上。例如 `apply le_trans x d (succ d) at hdl` 會把 `hdl : x ≤ d` 變成 `d ≤ succ d → x ≤ succ d`。改目標則用 `apply h`。",
        ),
    ),
    theorems = listOf(
        InventoryItem(InventoryKind.Theorem, "zero_le", "zero_le ", "`zero_le x`：`0 ≤ x`。"),
        InventoryItem(InventoryKind.Theorem, "add_zero", "add_zero", "`a + 0 = a`。"),
        InventoryItem(InventoryKind.Theorem, "add_succ", "add_succ", "`a + succ b = succ (a + b)`。"),
        InventoryItem(InventoryKind.Theorem, "succ_add", "succ_add", "`succ a + b = succ (a + b)`。"),
        InventoryItem(InventoryKind.Theorem, "add_assoc", "add_assoc", "`(a + b) + c = a + (b + c)`。"),
        InventoryItem(InventoryKind.Theorem, "succ_eq_add_one", "succ_eq_add_one", "`succ n = n + 1`。"),
        InventoryItem(
            InventoryKind.Theorem, "le_trans", "le_trans ",
            "`le_trans x y z`：若 `x ≤ y` 且 `y ≤ z`，則 `x ≤ z`。常寫成 `apply le_trans x d (succ d) at h`。",
        ),
        InventoryItem(
            InventoryKind.Theorem, "le_succ_self", "le_succ_self ",
            "`le_succ_self x`：`x ≤ succ x`。例如 `exact le_succ_self d`。",
        ),
    ),
    definitions = listOf(
        InventoryItem(InventoryKind.Definition, "ℕ", "ℕ", "本遊戲的自然數 `MyNat`：`0` 與 `succ`。"),
        InventoryItem(InventoryKind.Definition, "≤", "≤", "`a ≤ b` 定義為「存在 `c` 使得 `b = a + c`」。"),
        InventoryItem(InventoryKind.Definition, "∨", "∨", "邏輯或。證它用 `left` / `right`；用它用 `cases`。"),
    ),
    officialSolution = listOf(
        "induction y with d hd",
        "right",
        "exact zero_le x",
        "cases hd with h1 h2",
        "left",
        "cases h1 with e h1",
        "rw [h1]",
        "use e + 1",
        "rw [succ_eq_add_one, add_assoc]",
        "rfl",
        "cases h2 with e he",
        "cases e with a",
        "rw [he]",
        "left",
        "rw [add_zero]",
        "use 1",
        "exact succ_eq_add_one d",
        "right",
        "use a",
        "rw [add_succ] at he",
        "rw [succ_add]",
        "exact he",
    ),
)
