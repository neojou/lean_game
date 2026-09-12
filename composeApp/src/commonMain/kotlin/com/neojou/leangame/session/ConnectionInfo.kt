package com.neojou.leangame.session

enum class ConnectionStatus {
    Connecting,
    Ready,
    Error,
    MissingToolchain,
}

data class ConnectionInfo(
    val status: ConnectionStatus,
    val detailZh: String,
    val leanVersion: String? = null,
    val toolchainExpected: String? = null,
    val toolchainActual: String? = null,
    val lakePath: String? = null,
    val leanPath: String? = null,
    val mode: String? = null,
    val lastLspError: String? = null,
    val projectDir: String? = null,
) {
    companion object {
        fun connecting(): ConnectionInfo = ConnectionInfo(
            status = ConnectionStatus.Connecting,
            detailZh = "正在連本機 Lean…",
        )
    }
}
