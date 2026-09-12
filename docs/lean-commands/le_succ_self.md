# le_succ_self

## 概述

`le_succ_self x` 證明 `x ≤ succ x`。每個數都小於等於自己的後繼。

分類：定理 → **≤**

## 示例

### 1. `exact`（你要的那一行）

目標是 `d ≤ succ d` 時：

```lean
exact le_succ_self d
```

常接在 `apply le_trans x d (succ d) at hdl` 然後 `apply hdl` 之後。

### 2. 自己證一遍（不必，但有助理解）

依定義這就是 `∃ c, succ x = x + c`，見證是 `1`：

```lean
use 1
exact succ_eq_add_one x
```

背包裡有現成定理時，用 `exact le_succ_self d` 比較短。

## 細節

精確型別：`(x : ℕ) : x ≤ succ x`。

LessOrEqual 第 3 關解鎖。
