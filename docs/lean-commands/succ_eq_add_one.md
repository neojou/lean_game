# succ_eq_add_one

## 概述

`succ_eq_add_one n` 證明 `succ n = n + 1`。在「後繼」和「加一」之間翻譯。

分類：定理 → **+**

## 示例

### 1. `exact` 直接交卷

目標是 `succ d = d + 1` 時：

```lean
exact succ_eq_add_one d
```

官方解答在 `use 1` 之後用過。

### 2. `rw` 把 `succ` 換成 `+ 1`

```lean
rw [succ_eq_add_one]
```

或一次做完：

```lean
rw [succ_eq_add_one, add_assoc]
```

### 3. 反向

若你想把 `n + 1` 寫回 `succ n`：

```lean
rw [← succ_eq_add_one]
```

## 細節

精確型別：`(n : ℕ) : succ n = n + 1`。

因為本遊戲的加法是公理而不是定義，這條**不是** `rfl` 能證的，必須當定理用。
