# 指令說明（編譯期打包）

每個指令一個 Markdown 檔，檔名對應程式裡的 `docId`（通常就是按鈕上的英文名）。

| 檔名 | 按鈕 |
|---|---|
| `rfl.md`、`rw.md`、`use.md`… | 策略 |
| `add_zero.md`、`le_trans.md`… | 定理 |
| `nat.md`、`le.md`、`or.md` | 定義 `ℕ`、`≤`、`∨` |

**改完這些檔不會立刻出現在已開著的視窗。** 必須重新編譯／執行（`./gradlew :composeApp:run`）。Gradle 會在編譯時把內容嵌進 `LeanCommandDocs`，執行期**不**讀這個目錄。這是為了之後 KMP 多平台也能用同一套靜態資源。
