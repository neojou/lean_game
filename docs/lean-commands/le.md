# ≤

## 概述

`a ≤ b` 在本遊戲的**定義**是：存在自然數 `c` 使得 `b = a + c`。

也就是「`b` 比 `a` 多出某段差 `c`」（`c` 可以是 `0`，所以包含相等）。

## 示例

### 1. 證 `≤`：提出差

要證 `x ≤ succ d`，就是要找到 `c` 使 `succ d = x + c`。常見：

```lean
use e + 1
```

或

```lean
use 1
```

### 2. 用 `≤`：拆開

若有假設 `h : x ≤ d`：

```lean
cases h with e he
```

得到 `e : ℕ` 和 `he : d = x + e`。

### 3. 傳遞

已有 `x ≤ d` 又知道 `d ≤ succ d`（`le_succ_self`）時，用 `le_trans` 得到 `x ≤ succ d`。

## 細節

這不是 Lean 標準庫對 `Nat` 的 `≤`。不要用 `linarith` 或 `omega`；本關只用定義、`use`、`cases` 和已解鎖的定理。
