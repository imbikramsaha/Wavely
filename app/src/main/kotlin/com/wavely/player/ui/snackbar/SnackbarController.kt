package com.wavely.player.ui.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * App-wide snackbar access for reversible destructive actions (design.md §9: "Undo snackbar").
 * Provided by [com.wavely.player.ui.MainScreen] via [LocalSnackbarController].
 */
class SnackbarController(
    private val hostState: SnackbarHostState,
    private val scope: CoroutineScope,
) {
    /** Show a message with an "Undo" action; [onUndo] runs only if the user taps Undo. */
    fun showUndo(message: String, onUndo: () -> Unit) {
        scope.launch {
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = "Undo",
                withDismissAction = true,
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) onUndo()
        }
    }

    fun show(message: String) {
        scope.launch { hostState.showSnackbar(message = message, duration = SnackbarDuration.Short) }
    }
}

val LocalSnackbarController = staticCompositionLocalOf<SnackbarController> {
    error("No SnackbarController provided")
}
