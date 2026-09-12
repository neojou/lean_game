# AGENTS.md — Lean Game Desktop（KMP / Compose）

給 **Grok Build** 與後續 session 的工作契約。
讀完本檔再改程式。不要另開平行專案、不要做 wasm/JS、不要一次做完整 NNG。
**預設交付範圍是 P0→P3 一次做完**（可玩的第八關 Desktop 客戶端）。P4 與 GameServer 真 Hint 引擎不要做。使用者會在 P3 完成後才進來看操作體驗，過程中不必停下來等人確認。

來源 repo：https://github.com/neojou/lean_game
參考伺服器：https://github.com/leanprover-community/lean4game
參考關卡：https://adam.math.hhu.de/#/g/leanprover-community/nng4/world/LessOrEqual/level/8
關卡原始檔：https://github.com/leanprover-community/NNG4/blob/main/Game/Levels/LessOrEqual/L08le_total.lean

語言：繁體中文（UI 文案、提示、按鈕說明）。Tactic / theorem **名稱維持英文**。

---

## 0. 一句話目標

在既有 **Kotlin Multiplatform + Compose Desktop** 殼上，做一個只跑 Desktop 的視窗，操作盡量對齊 Lean Game Server 的 Typewriter 模式：左邊題目與提示、中間逐步下指令並看 goal 狀態、右邊 inventory 按鈕（英文指令名 + 繁中說明）。第一版只做 NNG4 `LessOrEqual` **第 8 關** `le_total`。後端用本機已安裝的 Lean 4，經 `lean --server` / `lake serve` 溝通，**不**做瀏覽器、不嵌入 Node relay、不發布到 adam.math.hhu.de。

---

## 1. 明確不做（除非使用者下一輪改契約）

- wasm / JS / Android / iOS target
- 世界地圖、多關卡解鎖、存檔雲端、帳號
- 完整 Editor mode（Monaco 整份證明檔）。第一版只做 Typewriter：一次送一行 tactic
- 完整移植 lean4game 的 Node `relay/` + React `client/`
- 在線下載 GitHub game、bubblewrap sandbox、多人 session queue
- 自己重寫 Lean kernel 或 GameServer DSL elaborator
- 把 `LeanGame.kt` 裡殘留的 K Chart / 資料庫選單當功能做（那是殼層複製痕跡，應清掉）

---

## 2. 現況（2026-09-12 實查）

`neojou/lean_game` 已是 KMP Desktop 殼，**還沒有遊戲與 Lean client**：

| 路徑 | 狀態 |
|---|---|
| `composeApp/` | Compose Multiplatform，只開 `jvm("desktop")` |
| `composeApp/src/desktopMain/.../Main.kt` | 視窗 960×640，title 用 `AppVersion.APP_NAME_EN` |
| `composeApp/src/commonMain/.../App.kt` | 啟動時 `SystemSettings.initOnce()` |
| `composeApp/src/commonMain/.../LeanGame.kt` | 頂欄 + 中間 `"LEAN Game"` 佔位 |
| Kotlin / Compose | Kotlin 2.3.0，Compose Multiplatform 1.10.0，JVM toolchain 25 |
| 字型 | `composeResources/font/notosanstc_regular.otf`（繁中可用） |

Web 版 lean4game 實際是三層，Desktop 版**不要**原樣搬：

```
React client  --WebSocket/LSP-->  Node relay  --stdin/stdout-->  Lean GameServer
                                      │
                                      └─ 把玩家 tactic 包進 Runner 命令
                                         並呼叫 RPC `Game.getProofState`
```

GameServer 專屬能力（第一版用較薄的替代，見 §4）：

- DSL：`World` / `Level` / `Statement` / `Hint` / `TheoremDoc` / `NewTactic`
- `Runner`：限制 inventory、掛關卡環境
- `Game.getProofState`：逐步 goals + 對應 Hint + diagnostics + completed
- `.lake/gamedata/*.json`：靜態關卡與 inventory

本機關卡數學內容（不要重寫數學，要引用或抽出最小依賴）：

```text
Statement: le_total (x y : ℕ) : x ≤ y ∨ y ≤ x
定義：a ≤ b := ∃ c, b = a + c
官方解答用到：
  induction, cases, left, right, use, rw, exact, rfl
  zero_le, add_zero, add_succ, succ_add, add_assoc, succ_eq_add_one
```

---

## 3. 目標架構（Desktop 第一版）

三個進程／模組，全在本機：

```
┌──────────────────────────────────────────────────────────┐
│  Compose Desktop UI  (commonMain UI + desktopMain I/O)   │
│  左：題目 / 對話 / hint                                   │
│  中：goal 狀態 + 已下指令 + typewriter 輸入列              │
│  右：Tactics / Theorems / Definitions 按鈕與說明           │
└──────────────┬───────────────────────────────────────────┘
               │ Kotlin 資料結構 ProofState
               │（不要把 LSP JSON 直接塞進 Composable）
               ▼
┌──────────────────────────────────────────────────────────┐
│  LeanSession（僅 desktopMain）                            │
│  啟動、重啟、didOpen/didChange、等 diagnostics、          │
│  抽 interactive goals、把結果映成 ProofState              │
└──────────────┬───────────────────────────────────────────┘
               │ LSP JSON-RPC over stdin/stdout
               ▼
┌──────────────────────────────────────────────────────────┐
│  本機 Lean 4                                             │
│  建議：在 vendored Lake 專案裡 `lake serve`               │
│  退路：`lean --server` + 正確 LEAN_PATH / .olean          │
└──────────────────────────────────────────────────────────┘
```

原則：

1. **UI 與協定分開。** `commonMain` 只認識 `ProofState` / `InventoryItem` / `LevelContent`。LSP、Process、路徑只活在 `desktopMain`。
2. **第一關先能玩通，再談通用 Game 載入器。** 關卡文案、hint、inventory 可以先寫死在 Kotlin（或一份 `level8.json`），Lean 只負責檢查證明。
3. **不要為了「跟網站 100% 同一套 GameServer RPC」卡住 UI。** 網站用 `Game.getProofState` 是因為有 Hint 匹配與 inventory 禁令；我們第一版用「硬編碼 hint + 標準 Infoview goals + 本機 inventory 表」就夠做出相同操作手感。
4. **之後若要完整 Hint 引擎 / 禁用 tactic**，再 vendor `lean4game/server`（GameServer Lake package）並對該專案 `lake serve`。那是 **P4**，本次不要做。

---

## 4. 後端策略（選一條主路，不要同時做三條）

### 主路（建議 Grok Build 採用）：最小 Lake playground + `lake serve`

在 repo 新增 `lean/nng_level8/`（名稱可調，不要放到 `composeApp` 裡）：

```
lean/nng_level8/
  lean-toolchain          -- 與使用者本機 Lean 4 對齊，寫死版本
  lakefile.toml
  Play.lean               -- 唯一給 LSP 開的檔
  MyNat/...               -- 只放本關需要的定義與已解鎖定理
```

`Play.lean` 形如（概念，不是叫你現在就寫完所有 lemma）：

```lean
import NngLevel8.Prelude   -- MyNat、≤、本關之前已有的定理

/-- 遊玩檔：UI 只改 `by` 後面的 tactic 行。 -/
theorem le_total (x y : ℕ) : x ≤ y ∨ y ≤ x := by
  sorry
```

Desktop 端流程：

1. 啟動時 `ProcessBuilder` 在該目錄跑 `lake serve`（或 `lean --server`，但 `lake serve` 較能處理依賴）。
2. LSP `initialize` → `initialized` → `textDocument/didOpen`（`Play.lean`）。
3. 玩家送出一行 tactic：改 `by` 區塊、`didChange`、等 `textDocument/publishDiagnostics`。
4. 用 Lean Infoview RPC 取 goals（見 §5）。不要自己 parse 純文字 goal。
5. 證明完成：diagnostics 無 error，且 goals 為空（或 `sorry` 已消失且無 unsolved goals）。
6. 進程掛掉要能重啟；一次只開一個 session。

本關需要事先存在、玩家**可在 inventory 引用**的定理（至少）：

- `zero_le`
- `add_zero` / `add_succ` / `succ_add` / `add_assoc`
- `succ_eq_add_one`
- （視 Prelude 而定）`le_refl` 等，即使本關官方解答沒用到，右邊面板仍可能顯示「到這一關為止已解鎖」的定理。第一版可只放本關解答真正會點到的子集，並在面板註明「精簡背包」。

數學定義不要自己發明。優先：

1. 把 NNG4 需要的模組 **subtree / 複製最小集合** 進 `lean/nng_level8`（注意 NNG 授權與 `MyNat` 命名空間）。
2. 或讓 `lakefile` 依賴本機／git 上的 NNG4，再寫一個**不含 GameServer DSL** 的薄 `Play.lean` `import` 已證定理。  
   注意：直接 `import Game.Levels.LessOrEqual.L08le_total` 會把官方證明一起編進來，玩家就沒有關可破。Play 檔必須是**未完成**的 statement。

### 副路（P4，本次不要做）

Vendor `lean4game/server` 當 Lake dependency，對 NNG4 跑 GameServer，實作 `Game.getProofState`。這才能自動匹配 `Hint (hidden := true)`。工作量大，且要處理 Runner 包 tactic、URI 改寫、行號平移。P3 交付後由使用者試玩，再決定要不要走這條。

### 禁止當正式後端的路

- 只在 Kotlin 裡模擬 tactic（沒有 Lean）：`FakeLeanSession` 只准當內部開發支架，**P3 交付物必須接真 Lean**。若本機找不到 `lake`/`lean`，App 仍要能打開，狀態列用繁中說明缺什麼、怎麼裝，不得假裝證明已通過。
- 每次 `echo tactic | lean` 冷啟動：太慢，狀態也不對。
- 呼叫網站 WebSocket：違反「本機 Desktop」且不穩。

---

## 5. Lean 溝通要點（desktopMain）

Lean language server 是 **LSP + JSON-RPC**，走 stdin/stdout，訊息格式：

```text
Content-Length: <n>\r\n
\r\n
<json>
```

必做：

- 正確處理 header、UTF-8 byte length、可能黏在一起的多則訊息
- 請求有 `id`；notification 無 `id`（`didOpen` / `didChange` / `publishDiagnostics`）
- 先 `initialize`，再 `initialized`，再打開檔案
- `didChange` 用 full document sync 即可（第一版不要做增量 diff）

Goals 取得（標準 Infoview，不必 GameServer）：

Lean 4 的 interactive goal 走 **`$/lean/rpc`**（或新版等價的 RPC session）。實作時以本機 `lean --server` 版本為準，對照：

- VS Code Lean 4 擴充的 `rpcApi.ts`
- lean4game `client/src/components/infoview/rpc_api.ts` 的 `ProofState` / `InteractiveGoal` 形狀

第一版可接受的降級：若 RPC widget 一時接不上，先用 `Lean.Widget.getInteractiveGoals` 失敗時退回 diagnostics 純文字。但 UI 資料模型仍要預留 hyps / target 分開顯示。

Kotlin 端建議資料結構（名稱可調，語意不要拆）：

```text
data class Hypothesis(val names: List<String>, val typePretty: String)
data class Goal(val hyps: List<Hypothesis>, val targetPretty: String, val hints: List<Hint>)
data class ProofStep(val command: String, val goals: List<Goal>, val diagnostics: List<String>)
data class ProofState(
  val steps: List<ProofStep>,   // steps[0] = 尚未下指令
  val completed: Boolean,
  val busy: Boolean,
  val serverError: String?,
)
```

Typewriter 語意（對齊網站）：

- 輸入一列，Enter 送出（可允許列尾可有可無 `,`；送給 Lean 時統一補格式）
- 成功：該列鎖進歷史、輸入框清空、顯示新 goals
- 失敗：該列不要鎖死；輸入框保留；在該步底下顯示錯誤（紅字）
- 歷史列可刪最後一步（Undo）或點某步「從這裡重來」（把後面 tactic 丟掉再 didChange）
- 證明完成：左／中顯示過關文案，禁止再送（或允許 Undo 回來）

---

## 6. UI 規格（對齊網站，繁中）

視窗預設建議 ≥ 1280×800（現在 960×640 太窄，三欄會擠）。可拉。

```
+---------------------------------------------------------------+
|  選單：遊戲 / 證明 / 說明                                      |
+------------------+---------------------------+----------------+
| 左 約 25%        | 中 約 50%                 | 右 約 25%      |
| 關卡標題         | 題目陳述（固定）           | 定理 / 策略 / 定義 |
| 引言             | ---------------------------| 定理子分類 + * ^ ≤ … |
| 對話 / hint      | 目前 goals                 | 指令按鈕（英文名） |
| 「顯示更多提示」  | 已送出的指令列             | 說明框（md 編譯進來）|
| 過關結語         | [ typewriter 輸入 ] 送出   | 點按鈕=顯示說明   |
+------------------+---------------------------+----------------+
| 狀態列：Lean 版本 / 連線 / 忙碌                               |
+---------------------------------------------------------------+
```

行為細節：

- **右欄分頁**（繁中，對齊 NNG 截圖）：**定理**、**策略**、**定義**。定理底下再依 `+` `*` `^` `≤` `012` `Peano` 分子分類顯示不同按鈕群。
- **右欄按鈕**：面上寫英文 tactic/theorem 名。**點一下只在下方說明框顯示該指令的說明，不要插入中間輸入框，不要另做 `?`。** 玩家自己在中欄打字。
- **說明來源**：`docs/lean-commands/<docId>.md`，一個指令一個檔，繁中，含概述與多情境示例。Gradle 編譯期嵌進 `LeanCommandDocs`（KMP 靜態資源）；**執行期不讀這個目錄**。改 md 後必須重新編譯才看得到。定義 `ℕ`/`≤`/`∨` 的檔名用 `nat.md` / `le.md` / `or.md`。
- **左欄**：先顯示引言。每一步若有對應 hint，追加在對話區。隱藏 hint 預設不出現，需按「顯示更多提示！」（對齊網站 "Show more help!"）。
- **中欄 goals**：每個 goal 先 hyps（`x y : ℕ`、`hd : …`）再 `⊢ target`。多 goal 時分塊，標「目標 1 / 2」。
- 字型用已有 Noto Sans TC；程式碼／Lean 符號用等寬（可再加 JetBrains Mono 或系統 monospace）。
- 不要做網頁那套 Monaco 雙模式切換（那是 P4）。

選單建議（取代 About-only 與 KChart 殘留）：

- 遊戲 → 重新開始本關、結束
- 證明 → 復原一步、顯示目前 Lean 檔
- 說明 → 關於、Lean 連線診斷

---

## 7. 第八關文案（第一版就用這些，不要再發明語氣）

標題：`x ≤ y 或 y ≤ x`  
世界：`≤ 世界`　關卡：8  
定理名（inventory / 過關後）：`le_total`

**陳述（顯示用）：**  
若 \(x\) 與 \(y\) 是自然數，則 \(x \le y\) 或 \(y \le x\)。

**Lean 目標初始：**

```text
x y : ℕ
⊢ x ≤ y ∨ y ≤ x
```

**引言：**

這大概是目前最硬的一關。提示：若 `a` 是一個數，`cases a with b` 會拆成 `a = 0` 與 `a = succ b` 兩種情形。  
在假設還沒保證你能證出結果之前，先別急著 `left` 或 `right`。

我留了隱藏提示；若需要，從頭再試一次，並點「顯示更多提示！」。

**隱藏提示（依官方解答節點；第一版可用「步數／目標形狀」粗配，不必做 GameServer 的 fvar bijection）：**

1. 開頭：先做 `induction y with d hd`。
2. 歸納步驟、面前是 `hd : x ≤ d ∨ d ≤ x`：試 `cases hd with h1 h2`。
3. 右支、`h2 : d ≤ x`：接著 `cases h2 with e he`。
4. 仍不知道該往左還是往右：對差做 `cases e with a`。

**結語：**

非常好。路過的數學家說：你剛剛證明了 `ℕ` 是全序的。這個世界剩下的關卡輕鬆多了。

官方解答（僅供測試「能過關」，不要當預填進輸入框）：

```lean
induction y with d hd
right
exact zero_le x
cases hd with h1 h2
left
cases h1 with e h1
rw [h1]
use e + 1
rw [succ_eq_add_one, add_assoc]
rfl
cases h2 with e he
cases e with a
rw [he]
left
rw [add_zero]
use 1
exact succ_eq_add_one d
right
use a
rw [add_succ] at he
rw [succ_add]
exact he
```

---

## 8. 右欄 inventory（第一版最小集）

指令名英文、說明繁中。v0.3 起點按鈕**只顯示** `docs/lean-commands/` 裡對應 md，不插入輸入框。下表「插入範本」僅供玩家手打參考，不是按鈕行為。

### Tactics

| 名稱 | 插入範本 | 說明（繁中） |
|---|---|---|
| `rfl` | `rfl` | 當目標兩邊**看起來完全一樣**時收掉等式。 |
| `rw` | `rw []` | `h : X = Y` 時，`rw [h]` 把目標裡的 `X` 改成 `Y`。反向用 `rw [← h]`；改假設用 `rw [h] at h2`。 |
| `exact` | `exact ` | 若手上的項／定理剛好就是目標，直接交出證明。 |
| `induction` | `induction y with d hd` | 對自然數歸納。零情形與 `succ d` 情形分開；`hd` 是歸納假設。 |
| `cases` | `cases h with a ha` | 依類型拆假設或變數。`ℕ` 拆成 `0` / `succ`；`∨` 拆成左右支；`∃` 拆出見證。 |
| `left` | `left` | 目標是 `P ∨ Q` 時，改證 `P`。沒把握就別先按。 |
| `right` | `right` | 目標是 `P ∨ Q` 時，改證 `Q`。 |
| `use` | `use ` | 目標是 `∃ c, ...` 時，提出見證。本遊戲裡 `a ≤ b` 就是 `∃ c, b = a + c`。 |
| `apply` | `apply  at ` | `apply t at h` 把定理用在假設上。 |

完整說明以 `docs/lean-commands/<name>.md` 為準（編譯期打包）。

### Theorems（本關常用）

| 名稱 | 插入範本 | 說明（繁中） |
|---|---|---|
| `zero_le` | `zero_le ` | `0 ≤ x`。分類 `≤`。 |
| `add_zero` | `add_zero` | `a + 0 = a`。分類 `+`。 |
| `add_succ` | `add_succ` | `a + succ b = succ (a + b)`。分類 `+`。 |
| `succ_add` | `succ_add` | `succ a + b = succ (a + b)`。分類 `+`。 |
| `add_assoc` | `add_assoc` | `(a + b) + c = a + (b + c)`。分類 `+`。 |
| `succ_eq_add_one` | `succ_eq_add_one` | `succ n = n + 1`。分類 `+`。 |
| `le_trans` | `le_trans ` | `x ≤ y → y ≤ z → x ≤ z`。分類 `≤`。 |
| `le_succ_self` | `le_succ_self ` | `x ≤ succ x`。分類 `≤`。 |

加法相關（`add_zero` 等）分類在定理子頁 `+`。本關 `*` `^` `012` `Peano` 可以是空的。

### Definitions

| 名稱 | 說明（繁中） |
|---|---|
| `ℕ` | 本遊戲的自然數 `MyNat`：`0` 與 `succ`。 |
| `≤` | `a ≤ b` 定義為「存在 `c` 使得 `b = a + c`」。 |
| `∨` | 邏輯或。證它用 `left` / `right`；用它用 `cases`。 |

第一版不必實作網站的「禁用未解鎖 tactic」；可在送出後靠 Lean 報錯。若要防玩家偷用 `sorry` / `admit` / `native_decide`，在 Kotlin 送出前做黑名單字串檢查。

---

## 9. 建議目錄（在 `lean_game` repo 長，不要另起爐灶）

```
lean_game/
  AGENTS.md                          -- 本檔（請從 artifacts 拷進 repo 根）
  composeApp/
    src/commonMain/kotlin/com/neojou/leangame/
      App.kt                         -- 保留啟動
      LeanGame.kt                    -- 改成三欄殼
      level/                         -- 關卡靜態資料
        LevelContent.kt
        Level8.kt                    -- 文案、hint、inventory
      proof/                         -- ProofState 與 reducer（純 Kotlin）
      ui/
        pane/LeftPane.kt
        pane/CenterPane.kt
        pane/RightPane.kt
        GoalView.kt
        TypewriterBar.kt
        InventoryPanel.kt
        markdown/SimpleMarkdown.kt
      session/
        LeanSession.kt               -- expect
  docs/lean-commands/                -- 指令說明（一個指令一個 md；編譯期打包）
    src/desktopMain/kotlin/com/neojou/leangame/
      Main.kt
      session/
        LeanSession.desktop.kt       -- Process + LSP
        LspClient.kt
        PlayFile.kt                  -- 組 Play.lean 文字
  lean/nng_level8/                   -- Lake 專案，給 lake serve
```

`commonMain` 可放 `expect class LeanSession`；實際 process 必須 `desktopMain actual`。

測試：

- 純 Kotlin：`ProofState` reducer（送出／失敗／undo／完成）不要開 Lean。
- 手動：用 §7 官方解答走一遍應過關。
- 連線診斷：選單裡顯示 `lean --version`、`lake serve` 是否起來、最後一則 LSP error。

---

## 10. Grok Build 工作節奏（一次做到 P3，不要停下來等人）

**預設任務：把 P0、P1、P2、P3 在同一個實作過程裡做完。**  
P0–P2 是內部施工順序，不是四次獨立交付。不要做到 P0 就停、不要請使用者確認再繼續。P4 禁止。

使用者會在你宣告 P3 完成後，自己編譯／開 Desktop 看操作體驗。因此結束時必須留下：

1. 可編譯的 Desktop 目標（`composeApp` desktop）
2. `lean/nng_level8` 可 `lake build` 的最小 Lake 專案
3. 啟動後能連本機 Lean；連不上時狀態列講人話
4. Typewriter 用 §7 官方解答可過關
5. 短的「怎麼在本機跑」說明（可寫在 repo 既有 README，或 `composeApp` 旁極短段落；不要另開一堆文件）

施工時仍建議 **P0→P1→P2→P3 這個順序寫程式**，以免 UI 與 LSP 纏在一起難除錯；但同一個 session 要一路做到 P3 Definition of done。

中途若缺本機 `lean` / `lake` / toolchain：

- 不要把整個任務停在「請使用者先安裝 Lean」
- 把 Lake 專案與 LSP client 寫完
- App 在找不到二進位時仍能開視窗，狀態列用繁中列出缺的命令與建議
- 然後繼續把 P2/P3 的 Typewriter、Undo、hint、過關結語做完

### P0 — 殼與第八關靜態 UI（內部第一步）

- 清掉 KChart 選單殘留與誤導註解
- 三欄 layout + 繁中文案 + 右欄指令說明（點按鈕顯示 md，不插入）
- `FakeLeanSession` 可以暫時存在，但不可當最終後端
- 內部檢查：視窗打開就能讀完引言、點 `induction` 插入文字、按「顯示更多提示」看到第一則隱藏 hint

### P1 — 本機 Lean session（內部第二步）

- `lean/nng_level8` 能 `lake build`（在有 Lean 的機器上）
- Desktop 啟動 `lake serve`（找不到則退 `lean --server`），initialize + didOpen `Play.lean`
- 狀態列顯示連線成功／失敗原因（找不到 lake、toolchain 不符、stdio 掛掉）
- 內部檢查：log 看得到 `initialize` result 與第一次 diagnostics；沒有 Lean 時至少有清楚錯誤字串

### P2 — Typewriter ↔ Lean（內部第三步）

- 送出一行 → 改檔 → didChange → 更新 goals／錯誤
- Undo、重新開始（把 `by` 區塊重設為空並同步；遊玩模式不要留下 `sorry`）
- `sorry` / `admit` / `native_decide` 黑名單（送出前擋）
- 內部檢查：官方解答可過關；`rw [nope]` 顯示 Lean 錯誤且不鎖步

### P3 — 體驗對齊（**本次交付終點**）

- 過關結語、完成狀態；完成後預設不能再送，Undo 可回來
- hint 粗配：至少四則官方隱藏提示在「合理節點」出現；「顯示更多提示！」可展開當下隱藏提示
- 視窗預設 ≥ 1280×800、三欄可捲動、Lean 程式碼等寬
- 忙碌轉圈：Lean 思考中不可重入送出
- 選單：重新開始、復原一步、關於、Lean 連線診斷
- Definition of done：
  - Desktop 目標能編譯
  - 有 Lean 的機器上，只靠 UI（含隱藏提示）理論上打得完本關
  - 用 §7 官方解答走一遍會出結語
  - 沒有 wasm/JS、沒有 Node relay、沒有 P4 範圍

### P4 — 本次不要做

- 接 GameServer 真 Hint 匹配與 inventory 禁令
- 更多關卡、世界地圖
- Editor mode
- wasm

---

## 11. 給 Grok Build 的開場指令

把本檔放在 `lean_game` repo 根目錄後，貼下面這一則即可（不要再拆成 P0/P1/P2 分開下）：

```text
讀 AGENTS.md。一次做到 P3 交付終點：第八關 Desktop Typewriter + 本機 lake serve／lean --server。
P0–P2 當內部施工順序，不要停下來等人確認。不要做 P4、不要加 wasm/JS、不要搬 Node relay。
使用者稍後會自己開視窗看操作體驗。結束前用 §13 清單自檢，並留下本機怎麼跑的最短說明。
```

若環境沒有 Lean，仍要把 Kotlin／Lake 專案與 LSP client 寫完，並讓 App 以繁中狀態列說明缺什麼；不要因此改去做 P4。

---

## 12. 實作約束

- 回應與 UI 字串用繁體中文；程式識別子可用英文。
- 不要把 Node、npm、React 加進本 repo 當執行期依賴。
- 不要在 `commonMain` import `java.lang.ProcessBuilder`。
- 不要提交 `.lake/`、`.olean`、本機絕對路徑。
- `lean-toolchain` 寫死版本；與開發機不一致時要在 UI 講清楚，不要默默編譯錯版本。
- 變更現有檔先讀完整檔。`LeanGame.kt` 是殼，直接改成遊戲主畫面，不要再疊一層「Home 佔位」。
- 數學敘述以 NNG4 原關為準，翻譯時不改題意。
- 授權：NNG4 / lean4game 程式碼若複製進來，保留原 license 標示，不要假裝是全新數學。

---

## 13. 驗收清單（P3 交付時由 Grok Build 自檢）

- [ ] Desktop 視窗三欄，文案繁中，指令名英文
- [ ] 右欄按鈕插入中欄輸入框（不立即送出）
- [ ] 「顯示更多提示！」展開官方隱藏提示；至少四則在合理節點可出現
- [ ] 本機 Lean 連得上；斷線／缺 lake／缺 toolchain 有繁中說明
- [ ] 逐步 tactic 後 goals 更新
- [ ] 錯誤 tactic 不鎖步
- [ ] Undo / 重新開始可用
- [ ] 官方解答可過關並顯示結語；完成後不能再送（Undo 可回來）
- [ ] Lean 忙碌時不可重入送出
- [ ] 預設視窗夠大、三欄可捲動、程式碼等寬
- [ ] `sorry` / `admit` / `native_decide` 遊玩模式送出前被擋
- [ ] 沒有 wasm/JS target、沒有 Node relay、沒做 P4

---

## 14. 參考（需要時再打開，不要一開始就整倉抄進 UI）

- lean4game 總覽：https://github.com/leanprover-community/lean4game
- 資料流（Runner、`Game.getProofState`、Typewriter）：https://deepwiki.com/leanprover-community/lean4game/2.1-data-flow-and-communication
- lean4game RPC 形狀：`client/src/components/infoview/rpc_api.ts`
- Typewriter UI：`client/src/components/infoview/typewriter.tsx`
- Inventory UI：`client/src/components/inventory/`
- GameServer：`server/GameServer/RpcHandlers.lean`、`Runner.lean`、`Hints.lean`
- NNG4 入口：https://github.com/leanprover-community/NNG4
- 本關：`Game/Levels/LessOrEqual/L08le_total.lean`
- 舊型 game server（stdin JSON 指令，概念單純但與現行網站不同）：https://github.com/PatrickMassot/lean4-game-server
