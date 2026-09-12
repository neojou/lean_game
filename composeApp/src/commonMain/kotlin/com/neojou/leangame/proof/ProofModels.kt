package com.neojou.leangame.proof

/**
 * Shared proof-state model. UI and session talk only through these types;
 * LSP JSON never enters a Composable.
 */
data class Hypothesis(
    val names: List<String>,
    val typePretty: String,
)

data class Hint(
    val id: String,
    val text: String,
    val hidden: Boolean,
)

data class Goal(
    val hyps: List<Hypothesis>,
    val targetPretty: String,
    val hints: List<Hint> = emptyList(),
    val userName: String? = null,
)

data class ProofStep(
    val command: String,
    val goals: List<Goal>,
    val diagnostics: List<String>,
)

data class ProofState(
    val steps: List<ProofStep>,
    val completed: Boolean,
    val busy: Boolean,
    val serverError: String?,
    val lastSubmitError: String? = null,
    val playFileText: String = "",
) {
    val currentGoals: List<Goal>
        get() = steps.lastOrNull()?.goals.orEmpty()

    val acceptedCommands: List<String>
        get() = steps.drop(1).map { it.command }

    val canSubmit: Boolean
        get() = !busy && !completed && serverError == null

    companion object {
        fun connecting(): ProofState = ProofState(
            steps = emptyList(),
            completed = false,
            busy = true,
            serverError = null,
        )
    }
}
