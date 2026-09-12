# right

## 概述

目標是 `P ∨ Q` 時，`right` 表示「我要證右邊的 `Q`」。目標會變成 `Q`。

## 示例

### 1. 零情形

`induction y` 之後的零情形目標是 `x ≤ 0 ∨ 0 ≤ x`。沒有任何 `x ≤ 0` 的保證，但永遠有 `0 ≤ x`：

```lean
right
exact zero_le x
```

### 2. 差是 `succ a` 的那支

官方解答在 `e = succ a` 時走右邊，證 `succ d ≤ x`：

```lean
right
use a
rw [add_succ] at he
rw [succ_add]
exact he
```

### 3. 和 `left` 一樣，選錯會卡住

只有 `x ≤ d` 時不要 `right` 去證 `succ d ≤ x`。

## 細節

- 對應 `Or.inr`。
- 不吃參數。
- 與 `left` 成對：先想清楚你要交 `∨` 的哪一邊。
