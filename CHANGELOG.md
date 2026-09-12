# Changelog

產品版本以 About 視窗的 `AppVersion.DISPLAY` 為準（目前 `v0.3`）。

## v0.3 — 2026-09-12

- 右欄改為 **定理 / 策略 / 定義**（繁中分頁）；定理底下再依 `+` `*` `^` `≤` `012` `Peano` 分子分類。
- 點指令按鈕只在右欄說明框顯示內容，**不再**插入中間輸入框，也拿掉 `?`。
- 說明來源：`docs/lean-commands/<指令>.md`（一個指令一個檔）。Gradle 在**編譯期**嵌進 `LeanCommandDocs`，執行期不讀磁碟；改 md 後需重新編譯。
- 策略說明加長：概述之外含多種情境示例（如 `use` 的存在量詞、`≤`、選錯見證）。

## v0.2 — 2026-09-12

- About 顯示版本改為 **v0.2**（`AppVersion` / `composeApp` Gradle `0.2.0`）。
- 第八關背包補上已解鎖定理 **`le_trans`**，並支援 NNG 風格 `apply … at`（例如 `apply le_trans x d (succ d) at hdl`）。
- 第八關背包補上 **`le_succ_self`**，可下 `exact le_succ_self d`。

## v0.1 — 2026-09-12

- 第一版可玩的 Desktop Typewriter：NNG4 `≤ 世界` 第 8 關 `le_total`。
- 三欄 UI（題目／goals／inventory）、繁中文案、本機 `lake serve`／`lean --server`。
- 官方解答可過關；`sorry`／`admit`／`native_decide` 遊玩模式送出前會擋。
