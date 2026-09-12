# induction

## 概述

對自然數做數學歸納。本遊戲沿用 NNG 的 Lean 3 風格：

```lean
induction y with d hd
```

會把「關於 `y` 的目標」拆成：

1. **零情形**：`y` 換成 `0`（沒有 `d`、`hd`）
2. **後繼情形**：假設對 `d` 成立（歸納假設 `hd`），去證對 `succ d` 成立

## 示例

### 1. 本關開頭

目標是 `x ≤ y ∨ y ≤ x` 時，官方提示：

```lean
induction y with d hd
```

之後會得到兩個 goal：

- 零情形：`⊢ x ≤ 0 ∨ 0 ≤ x`（通常 `right` 再 `exact zero_le x`）
- 後繼：`hd : x ≤ d ∨ d ≤ x`，目標 `x ≤ succ d ∨ succ d ≤ x`

### 2. 名字可以自己取

```lean
induction n with k hk
```

第二個名字是歸納假設，第一個是「小一號的那個自然數」。

### 3. 歸納誰？

本關對 `y` 歸納比較順。對 `x` 歸納也能做，但後面的 `cases` 形狀會不一樣，和官方隱藏提示對不上。

## 細節

- 必須是 `ℕ`（本遊戲的 `MyNat`）上的變數。
- `with` 後面兩個名字：後繼情形的變數、歸納假設。
- 零情形的 `0` 會顯示成 `0` 而不是 `MyNat.zero`。

## 本遊戲的實作

底層用 `MyNat.rec'`，語法對齊 NNG 的 `induction n with d hd`，不是 Lean 4 教科書的

```lean
induction y with
| zero => …
| succ d hd => …
```

在 Typewriter 裡一次只送一行，所以用 `with d hd` 這種「先拆成多個 goal」的寫法。
