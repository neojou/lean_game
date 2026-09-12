# add_succ

## 概述

`add_succ a d` 證明 `a + succ d = succ (a + d)`。把右邊的 `succ` 搬到加號外面。

分類：定理 → **+**

## 示例

### 1. 改假設裡的 `+ succ`

官方解答：

```lean
rw [add_succ] at he
```

若 `he` 含有 `d + succ a` 這種形狀，會變成 `succ (d + a)`。

### 2. 改目標

```lean
rw [add_succ]
```

目標裡的 `x + succ n` 變成 `succ (x + n)`。

### 3. 和 `succ_add` 成對

- `add_succ`：succ 在 **右邊** `a + succ d`
- `succ_add`：succ 在 **左邊** `succ a + b`

弄反會 `rw` 失敗。

## 細節

精確型別：`(a d : ℕ) : a + succ d = succ (a + d)`。
