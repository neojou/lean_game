# succ_add

## 概述

`succ_add a b` 證明 `succ a + b = succ (a + b)`。把左邊的 `succ` 搬到加號外面。

分類：定理 → **+**

## 示例

### 1. 官方解答

在右支證 `succ d ≤ x` 時：

```lean
rw [succ_add]
```

用來把 `succ d + a` 對齊成 `succ (d + a)`，才能和假設裡的等式接上。

### 2. 和 `add_succ` 對照

目標或假設是 `succ d + a` 用 `succ_add`；是 `d + succ a` 用 `add_succ`。

## 細節

精確型別：`(a b : ℕ) : succ a + b = succ (a + b)`。

這不是加法的定義，而是本遊戲裡已經解鎖的定理（更早的世界證過）。
