package com.neojou.leangame.session

import com.neojou.leangame.proof.ProofState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

/**
 * Typewriter session. Process / LSP live in desktopMain actual.
 */
interface LeanSession {
    val proof: StateFlow<ProofState>
    val connection: StateFlow<ConnectionInfo>

    fun start()
    fun submit(command: String)
    fun undo()
    fun restart()
    fun restartFrom(stepIndex: Int)
    fun close()
}

expect fun createLeanSession(scope: CoroutineScope): LeanSession

expect fun requestAppExit()
