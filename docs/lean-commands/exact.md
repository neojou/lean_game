# exact

## 概述

當你手上的項或定理**剛好就是目前目標**時，用 `exact` 交出去。它不會再幫你推導缺口；對不上就失敗。

## 示例

### 1. 定理剛好是目標

歸納的零情形目標是 `x ≤ 0 ∨ 0 ≤ x` 的右支 `0 ≤ x` 時：

```lean
exact zero_le x
```

`zero_le x` 的型別就是 `0 ≤ x`。

### 2. `le_succ_self`

目標是 `d ≤ succ d` 時：

```lean
exact le_succ_self d
```

### 3. 假設剛好是目標

若有 `he : x = succ d + a`，而目標也是這條等式：

```lean
exact he
```

官方解答最後一步就是這種。

### 4. 等式定理當證明

```lean
exact succ_eq_add_one d
```

當目標是 `succ d = d + 1` 時成立。

### 5. 對不上時

`exact zero_le x` 在目標是 `x ≤ succ d` 時會失敗——型別不是同一個命題。這時該用 `apply`、`rw` 或 `use`，不是 `exact`。

## 細節

- 語法：`exact t`。
- 和 `apply` 的差別：`apply` 可以留下未填的前提當新目標；`exact` 要求一次交完。
- 本關常用：`exact zero_le x`、`exact le_succ_self d`、`exact he`、`exact succ_eq_add_one d`。
