# cases

## 概述

依型別把假設或變數拆開。本遊戲同樣用 NNG 風格的 `with` 取名。

## 示例

### 1. 拆 `∨`（或）

歸納假設 `hd : x ≤ d ∨ d ≤ x`：

```lean
cases hd with h1 h2
```

變成兩個 goal：

- 左支：`h1 : x ≤ d`
- 右支：`h2 : d ≤ x`

### 2. 拆 `≤`（其實是 `∃`）

`h1 : x ≤ d` 定義上是 `∃ e, d = x + e`：

```lean
cases h1 with e h1
```

得到見證 `e : ℕ` 和等式 `h1 : d = x + e`。注意後面那個名字可以沿用 `h1`，舊的 `h1` 會被換掉。

右支的 `h2 : d ≤ x` 同理：

```lean
cases h2 with e he
```

### 3. 拆自然數 `ℕ`

引言說的：`cases a with b` 把 `a` 分成 `0` 與 `succ b`。官方解答對差 `e` 做：

```lean
cases e with a
```

兩個 goal：

- `e` 是 `0`
- `e` 是 `succ a`

還不知道該 `left` 還是 `right` 時，常常就是先拆這個差。

### 4. 拆錯對象

對一個等式做 `cases h with a ha` 通常不是你要的。先看假設的型別是 `∨`、`≤` 還是 `ℕ`。

## 細節

- `∨`：`with` 兩個名字，兩個 goal。
- `∃` / `≤`：`with` 兩個名字（見證、性質），通常仍是一個 goal。
- `ℕ`：`with` 一個名字給 `succ` 那邊的前驅。

## 本遊戲的實作

語法對齊 NNG，不是 Lean 4 的 `| inl h =>` 寫法。Typewriter 一次一行，拆完用後續指令分別打兩個 goal。
