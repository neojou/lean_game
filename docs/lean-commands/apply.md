# apply

## 概述

`apply` 把一個 implication／定理「對準」目標或假設。

- 改**目標**：`apply h`（Lean 核心就有）
- 改**假設**（NNG 風格、本關需要）：`apply t at h`

本關典型用法是把 `le_trans` 用在已經有的 `≤` 假設上。

## 示例

### 1. `apply … at` 用在假設（forward）

若 `hdl : x ≤ d`，而下了：

```lean
apply le_trans x d (succ d) at hdl
```

`le_trans x d (succ d)` 的型別是 `x ≤ d → d ≤ succ d → x ≤ succ d`。套到 `hdl` 之後，`hdl` 變成：

```text
hdl : d ≤ succ d → x ≤ succ d
```

接著若目標是 `x ≤ succ d`，可以 `apply hdl`，再證 `d ≤ succ d`（例如 `exact le_succ_self d`）。

### 2. `apply` 用在目標（backward）

若 `hdl : d ≤ succ d → x ≤ succ d` 且目標是 `x ≤ succ d`：

```lean
apply hdl
```

目標變成 `d ≤ succ d`。

### 3. 和 `exact` 的差別

`exact le_succ_self d` 要求定理**正好**是目標。`apply le_succ_self` 在只需填參數時也可能成功；有多餘前提時會留下新 goal。本關對 `le_succ_self` 用 `exact` 最乾淨。

## 細節

- `apply t at h`：從 `t` 的前提裡找出與 `h` 型別相符的那一個，套上去，必要時把前面的參數變成新 goal。
- 假設名字要打對（本關常見 `hdl`、`h1`、`h2`，以你 `cases` / `induction` 時取的名為準）。

## 本遊戲的實作

`apply t at h` 是為了對齊 NNG，不依賴 Mathlib。`apply t`（沒有 `at`）仍是 Lean 核心的 `apply`。
