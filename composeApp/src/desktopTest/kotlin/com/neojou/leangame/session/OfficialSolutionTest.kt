package com.neojou.leangame.session

import com.neojou.leangame.level.Level8
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class OfficialSolutionTest {
    @Test
    fun officialSolutionCompletesWithLocalLean() = runBlocking {
        val lake = LeanPaths.findExecutable("lake")
        val lean = LeanPaths.findExecutable("lean")
        val project = LeanPaths.findNngProject()
        if (lake == null && lean == null || project == null) {
            println("SKIP: 本機沒有 lake/lean 或找不到 nng_level8")
            return@runBlocking
        }
        val session = DesktopLeanSession(this)
        try {
            session.start()
            withTimeout(120_000) {
                while (session.connection.value.status == ConnectionStatus.Connecting) {
                    delay(100)
                }
            }
            assertEquals(
                ConnectionStatus.Ready,
                session.connection.value.status,
                session.connection.value.detailZh,
            )
            for (line in Level8.officialSolution) {
                val before = session.proof.value.acceptedCommands.size
                session.submit(line)
                withTimeout(60_000) {
                    while (
                        session.proof.value.acceptedCommands.size == before &&
                        session.proof.value.lastSubmitError == null
                    ) {
                        delay(40)
                    }
                }
                assertNull(
                    session.proof.value.lastSubmitError,
                    "官方解答停在 `$line`：${session.proof.value.lastSubmitError}",
                )
            }
            withTimeout(30_000) {
                while (!session.proof.value.completed && session.proof.value.busy) {
                    delay(40)
                }
            }
            assertTrue(session.proof.value.completed, "官方解答走完應顯示過關")
            assertTrue(!session.proof.value.canSubmit, "完成後不能再送")
        } finally {
            session.close()
        }
    }

    @Test
    fun badRewriteDoesNotLockStep() = runBlocking {
        val lake = LeanPaths.findExecutable("lake")
        val lean = LeanPaths.findExecutable("lean")
        val project = LeanPaths.findNngProject()
        if (lake == null && lean == null || project == null) {
            println("SKIP: 本機沒有 lake/lean 或找不到 nng_level8")
            return@runBlocking
        }
        val session = DesktopLeanSession(this)
        try {
            session.start()
            withTimeout(120_000) {
                while (session.connection.value.status == ConnectionStatus.Connecting) {
                    delay(100)
                }
            }
            assertEquals(ConnectionStatus.Ready, session.connection.value.status)
            session.submit("rw [nope]")
            withTimeout(60_000) {
                while (
                    session.proof.value.lastSubmitError == null &&
                    session.proof.value.acceptedCommands.isEmpty()
                ) {
                    delay(40)
                }
            }
            assertNotNull(session.proof.value.lastSubmitError) {
                "應留下 Lean 錯誤且不鎖步，commands=${session.proof.value.acceptedCommands}"
            }
            assertTrue(session.proof.value.acceptedCommands.isEmpty())
        } finally {
            session.close()
        }
    }
}
