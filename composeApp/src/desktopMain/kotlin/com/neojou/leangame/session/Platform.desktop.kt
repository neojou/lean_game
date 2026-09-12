package com.neojou.leangame.session

import kotlinx.coroutines.CoroutineScope
import kotlin.system.exitProcess

actual fun createLeanSession(scope: CoroutineScope): LeanSession = DesktopLeanSession(scope)

actual fun requestAppExit() {
    exitProcess(0)
}
