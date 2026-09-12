# zero_le

## 概述

`zero_le x` 證明 `0 ≤ x`。每個自然數都大於等於 0。

分類：定理 → **≤**

## 示例

### 1. 本關零情形

```lean
right
exact zero_le x
```

目標從 `x ≤ 0 ∨ 0 ≤ x` 走到右支 `0 ≤ x`，剛好就是這條定理。

### 2. `apply` / 當 implication 的結論

較少見；本關用 `exact zero_le x` 即可。

## 細節

精確型別：`(x : ℕ) : 0 ≤ x`。

依定義 `0 ≤ x` 是 `∃ c, x = 0 + c`。你不必自己 `use`，這條定理已經證過。
