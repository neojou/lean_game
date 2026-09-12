# ∨

## 概述

`P ∨ Q` 是邏輯或：至少一邊為真。

- **證明**它：選一邊，用 `left`（證 `P`）或 `right`（證 `Q`）。
- **使用**它：`cases h with hP hQ`，拆成兩個 goal。

## 示例

### 1. 本關目標

```text
⊢ x ≤ y ∨ y ≤ x
```

全序：兩個數一定有一個比較小（或相等）。你要在每個分支決定走左還是走右。

### 2. 證或

零情形走右邊：

```lean
right
exact zero_le x
```

已經有 `x ≤ d` 時走左邊，把結論加強成 `x ≤ succ d`。

### 3. 用或

```lean
cases hd with h1 h2
```

`h1` 是左支、`h2` 是右支，後面的指令一次只打一個 goal。

## 細節

不要在資訊不夠時亂 `left` / `right`。引言：假設還沒保證你能證出結果之前，先 `cases` 或 `induction`。
