# add_assoc

## 概述

`add_assoc a b c` 證明 `(a + b) + c = a + (b + c)`。加法可以重新括號。

Lean 裡 `a + b + c` 印出來通常已經是左結合，也就是 `(a + b) + c`。

分類：定理 → **+**

## 示例

### 1. 官方解答

```lean
use e + 1
rw [succ_eq_add_one, add_assoc]
rfl
```

`use e + 1` 之後兩邊的括號可能不一樣，`add_assoc` 把它們調成同一個項，再 `rfl`。

### 2. 只改一處時可給參數

```lean
rw [add_assoc x a b]
```

需要精準指定時再用；多數時候 `rw [add_assoc]` 就夠。

## 細節

精確型別：`(a b c : ℕ) : a + b + c = a + (b + c)`。
