# LEAN Game

KMP Compose **Desktop** 客戶端，第一版只做 NNG4 `≤ 世界` 第 8 關 `le_total` 的 Typewriter 模式。本機 Lean 經 `lake serve`（找不到則 `lean --server`）檢查證明。沒有 wasm/JS、沒有 Node relay。

## 本機怎麼跑

1. 安裝 [elan / Lean 4](https://lean-lang.org/install/)（此關卡 toolchain：`leanprover/lean4:v4.33.1`）。
2. 確認 `~/.elan/bin` 在 PATH。
3. 建 Lake 專案（第一次較久）：

```bash
cd lean/nng_level8 && lake build
```

4. 開 Desktop 視窗：

```bash
./gradlew :composeApp:run
```

視窗中欄輸入 tactic 後 Enter 送出。右欄點按鈕看說明（不會插入輸入框）；說明寫在 `docs/lean-commands/`，改完需重新編譯。官方解答見 `AGENTS.md` §7。連不上時看選單「說明 → Lean 連線診斷」。
