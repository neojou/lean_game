# left

## 概述

目標是 `P ∨ Q` 時，`left` 表示「我要證左邊的 `P`」。目標會變成 `P`。

引言再三提醒：假設還沒保證你能證出結果之前，先別急著 `left` 或 `right`。

## 示例

### 1. 已經有 `x ≤ d`，要證 `x ≤ succ d ∨ …`

歸納的左支，官方解答：

```lean
left
```

然後把 `x ≤ succ d` 用 `use` / `rw` 證完。

### 2. `e = 0` 的那支

差是 0 時，其實 `x = d`，可以往左走證 `x ≤ succ d`：

```lean
left
rw [add_zero]
use 1
exact succ_eq_add_one d
```

### 3. 選錯邊

若你只有 `d ≤ x` 卻 `left` 去證 `x ≤ succ d`，後面會卡住。這時復原，改 `right` 或先 `cases`。

## 細節

- 只適用於恰好兩個建構子的歸納型，本關就是 `∨`。
- `left` 對應 `Or.inl`。
- 不吃參數。
