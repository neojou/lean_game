# le_trans

## 概述

`le_trans x y z` 證明：若 `x ≤ y` 且 `y ≤ z`，則 `x ≤ z`。也就是 `≤` 的傳遞性。

型別可以想成：

```text
x ≤ y → y ≤ z → x ≤ z
```

分類：定理 → **≤**

## 示例

### 1. 用在假設上（本關常見）

若 `hdl : x ≤ d`，想得到「再走一步到 `succ d`」：

```lean
apply le_trans x d (succ d) at hdl
```

`hdl` 變成 `d ≤ succ d → x ≤ succ d`。接著：

```lean
apply hdl
exact le_succ_self d
```

### 2. 三個參數是哪三個數

`le_trans x d (succ d)` 對應鏈條 `x ≤ d ≤ succ d`，所以結論是 `x ≤ succ d`。參數順序是「小、中、大」那三個端點，不是假設的名字。

### 3. 不要和 `exact` 搞混

目標若還不是 `x ≤ z`，而是 implication，應 `apply` 而不是 `exact le_trans …`。

## 細節

精確型別：`(x y z : ℕ) : x ≤ y → y ≤ z → x ≤ z`。

這是 LessOrEqual 第 4 關就解鎖的定理。
