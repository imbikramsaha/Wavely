package com.wavely.player.ui

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.Column
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.wavely.domain.model.PlaybackUiState
import com.wavely.player.navigation.AppNavigationRail
import com.wavely.player.navigation.BottomNavBar
import com.wavely.player.navigation.Destination
import com.wavely.player.navigation.WavelyNavHost
import com.wavely.player.ui.components.MiniPlayer
import com.wavely.player.ui.player.NowPlayingCallbacks
import com.wavely.player.ui.player.NowPlayingContent
import com.wavely.player.ui.player.NowPlayingTwoPane
import com.wavely.player.ui.player.PlayerViewModel
import com.wavely.player.ui.player.QueueSheet
import com.wavely.player.ui.player.SleepTimerDialog
import com.wavely.player.ui.player.nextSpeed
import com.wavely.player.ui.snackbar.LocalSnackbarController
import com.wavely.player.ui.snackbar.SnackbarController
import com.wavely.player.ui.theme.Dimens
import com.wavely.player.ui.theme.Spacing
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/** Drag velocity (px/s) past which a flick maximizes/minimizes the Now Playing sheet. */
private const val FLICK_VELOCITY = 1000f

/**
 * Drag events funneled into [PortraitSheet]'s single Animatable-owning consumer so that
 * per-delta snaps and the gesture-end settle can never race each other (see PortraitSheet).
 */
private sealed interface DragEvent {
    /** A drag delta in px to fold into the expansion fraction. */
    data class Delta(val delta: Float) : DragEvent
    /** Gesture released with this fling velocity (px/s); triggers the terminal settle. */
    data class Settle(val velocity: Float) : DragEvent
}

/**
 * Single-Activity adaptive root — design.md §8.
 *  - Compact  : bottom nav + (portrait expanding sheet | landscape two-pane).
 *  - Medium   : navigation rail + (portrait expanding sheet | landscape two-pane).
 *  - Expanded : navigation rail + persistent Now Playing side panel (no sheet).
 *
 * The portrait sheet animation is driven by a drag fraction, not a shared-element
 * transition (§5.3, risk T-2). Predictive back collapses the sheet (risk T-6).
 */
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val playerViewModel: PlayerViewModel = hiltViewModel()
    val state by playerViewModel.state.collectAsStateWithLifecycle()
    val favorites by playerViewModel.favorites.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarController = remember(snackbarHostState, scope) { SnackbarController(snackbarHostState, scope) }

    LifecycleAware(onActive = { active ->
        playerViewModel.onUiActive(active)
        if (!active) playerViewModel.persistNow() // save on background (§10.3)
    })

    // Surface skipped-track / playback errors as a snackbar (Stage 6 edge cases).
    LaunchedEffect(Unit) {
        playerViewModel.errorEvents.collect { snackbarController.show(it) }
    }

    // Survive rotation: remember whether Now Playing was open and restore it (Stage 5 exit).
    var savedExpanded by rememberSaveable { mutableStateOf(false) }
    val expansion = remember { Animatable(if (savedExpanded) 1f else 0f) }
    val fraction = expansion.value
    val expanded = fraction > 0.5f
    LaunchedEffect(expanded) { savedExpanded = expanded }
    var showSleepDialog by rememberSaveable { mutableStateOf(false) }
    var showQueue by rememberSaveable { mutableStateOf(false) }

    val isFavorite = state.current?.let { favorites.contains(it.identity.asKey()) } ?: false
    val openSettings: () -> Unit = { navController.navigate(Destination.Settings.route) }
    // System navigation-bar inset — the collapsed sheet must clear it AND the app bottom nav,
    // otherwise the mini-player overlaps and hides the bottom nav while a track plays.
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    // Measure the real bottom-nav height (Material3 NavigationBar is ~80dp + the nav inset it
    // applies itself) instead of guessing, so the collapsed sheet reserves exactly that much.
    val density = LocalDensity.current
    var bottomBarHeightPx by remember { mutableStateOf(0) }
    val bottomBarReserve = if (bottomBarHeightPx > 0) with(density) { bottomBarHeightPx.toDp() } else navBottom + 80.dp

    val callbacks = NowPlayingCallbacks(
        onPlayPause = playerViewModel::togglePlayPause,
        onNext = playerViewModel::next,
        onPrevious = playerViewModel::previous,
        onSeek = playerViewModel::seekTo,
        onShuffle = playerViewModel::toggleShuffle,
        onRepeat = playerViewModel::cycleRepeat,
        onSpeed = { playerViewModel.setSpeed(nextSpeed(state.speed)) },
        onSleep = { showSleepDialog = true },
        onToggleFavorite = { state.current?.let(playerViewModel::toggleFavorite) },
        onShowQueue = { showQueue = true },
    )

    CompositionLocalProvider(LocalSnackbarController provides snackbarController) {
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val widthClass = widthClassOf(maxWidth)
            val isLandscape = maxWidth > maxHeight

            if (widthClass == WindowWidth.EXPANDED) {
                ExpandedLayout(
                    navController = navController,
                    playerViewModel = playerViewModel,
                    state = state,
                    isFavorite = isFavorite,
                    callbacks = callbacks,
                    onSettings = openSettings,
                )
            } else {
                val useRail = widthClass == WindowWidth.MEDIUM

                // Predictive back collapses the expanded sheet/overlay (risk T-6).
                // Gate on `fraction > 0f`, NOT on `expanded` (fraction > 0.5f): gating on the
                // midpoint disabled the handler the instant a collapse crossed 0.5, cancelling
                // the in-flight gesture so it either bounced back open (catch → animateTo(1f))
                // or stuck. Staying enabled across the whole 1f→0f range lets it finish cleanly.
                PredictiveBackHandler(enabled = fraction > 0f) { progress ->
                    try {
                        progress.collect { event ->
                            expansion.snapTo((1f - event.progress).coerceIn(0f, 1f))
                        }
                        expansion.animateTo(0f)
                    } catch (_: CancellationException) {
                        expansion.animateTo(1f)
                    }
                }

                Row(modifier = Modifier.fillMaxSize()) {
                    if (useRail) {
                        AppNavigationRail(
                            navController = navController,
                            onSettings = openSettings,
                            modifier = Modifier.systemBarsPadding().displayCutoutPadding(),
                        )
                    }
                    BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        // ----- Content + (compact) bottom nav -----
                        Column(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.weight(1f).statusBarsPadding()) {
                                WavelyNavHost(
                                    navController = navController,
                                    playerViewModel = playerViewModel,
                                    bottomPadding = Spacing.md,
                                )
                            }
                            if (!useRail) {
                                if (state.hasTrack && !isLandscape) Spacer(Modifier.height(Dimens.miniPlayerHeight))
                                BottomNavBar(
                                    navController = navController,
                                    onReselect = { },
                                    // NavigationBar applies the nav-bar inset itself (default
                                    // windowInsets); we measure the resulting height for the sheet reserve.
                                    modifier = Modifier
                                        .onSizeChanged { bottomBarHeightPx = it.height }
                                        .graphicsLayer {
                                            if (!isLandscape) {
                                                translationY = fraction * size.height
                                                alpha = (1f - fraction * 1.5f).coerceIn(0f, 1f)
                                            }
                                        },
                                )
                            }
                        }

                        // ----- Player presentation -----
                        if (state.hasTrack) {
                            if (isLandscape) {
                                LandscapePlayer(
                                    fraction = fraction,
                                    state = state,
                                    isFavorite = isFavorite,
                                    callbacks = callbacks,
                                    bottomReserve = if (useRail) navBottom else bottomBarReserve,
                                    onExpand = { scope.launch { expansion.animateTo(1f) } },
                                    onPlayPause = playerViewModel::togglePlayPause,
                                    onNext = playerViewModel::next,
                                )
                            } else {
                                PortraitSheet(
                                    expansion = expansion,
                                    fraction = fraction,
                                    state = state,
                                    isFavorite = isFavorite,
                                    callbacks = callbacks,
                                    // Collapsed sheet sits above the system nav AND (compact) the app bottom nav.
                                    bottomReserve = if (useRail) navBottom else bottomBarReserve,
                                    onPlayPause = playerViewModel::togglePlayPause,
                                    onNext = playerViewModel::next,
                                    scope = scope,
                                )
                            }
                        }
                    }
                }
            }

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = Dimens.bottomNavHeight + if (state.hasTrack) Dimens.miniPlayerHeight else 0.dp),
            )
        }
        }

        if (showSleepDialog) {
            SleepTimerDialog(
                active = state.sleepTimerEndMs != null,
                nowMs = { System.currentTimeMillis() },
                onPick = { end ->
                    playerViewModel.setSleepTimer(end)
                    showSleepDialog = false
                },
                onDismiss = { showSleepDialog = false },
            )
        }

        if (showQueue) {
            QueueSheet(
                state = state,
                onSkipTo = playerViewModel::skipToQueueIndex,
                onMove = playerViewModel::moveQueueItem,
                onRemove = playerViewModel::removeFromQueue,
                onUndoRemove = playerViewModel::reinsertIntoQueue,
                onDismiss = { showQueue = false },
            )
        }
    }
}

/** Expanded width: rail + content + persistent Now Playing panel (§8.2). */
@Composable
private fun ExpandedLayout(
    navController: NavHostController,
    playerViewModel: PlayerViewModel,
    state: PlaybackUiState,
    isFavorite: Boolean,
    callbacks: NowPlayingCallbacks,
    onSettings: () -> Unit,
) {
    Row(modifier = Modifier.fillMaxSize().systemBarsPadding().displayCutoutPadding()) {
        AppNavigationRail(navController = navController, onSettings = onSettings)
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            WavelyNavHost(
                navController = navController,
                playerViewModel = playerViewModel,
                bottomPadding = Spacing.md,
            )
        }
        if (state.hasTrack) {
            Surface(
                modifier = Modifier.width(400.dp).fillMaxHeight(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
            ) {
                NowPlayingContent(
                    state = state,
                    isFavorite = isFavorite,
                    callbacks = callbacks,
                    showDragHandle = false,
                )
            }
        }
    }
}

/** Compact/medium landscape: docked mini-player ⇄ full-screen two-pane (risk T-5). */
@Composable
private fun BoxWithConstraintsScope.LandscapePlayer(
    fraction: Float,
    state: PlaybackUiState,
    isFavorite: Boolean,
    callbacks: NowPlayingCallbacks,
    bottomReserve: androidx.compose.ui.unit.Dp,
    onExpand: () -> Unit,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
) {
    // Mini-player docked at the bottom (above the bottom nav, if any) while collapsed.
    if (fraction < 1f) {
        MiniPlayer(
            state = state,
            onTap = onExpand,
            onPlayPause = onPlayPause,
            onNext = onNext,
            // bottomReserve already includes the system-nav inset (it's the measured bottom-nav height).
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = bottomReserve)
                .graphicsLayer { alpha = (1f - fraction * 2f).coerceIn(0f, 1f) },
        )
    }
    // Full-screen two-pane while expanding/expanded.
    if (fraction > 0f) {
        NowPlayingTwoPane(
            state = state,
            isFavorite = isFavorite,
            callbacks = callbacks,
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .displayCutoutPadding()
                .graphicsLayer { alpha = fraction },
        )
    }
}

/** Compact/medium portrait: the expanding bottom sheet (mini-player ⇄ Now Playing). */
@Composable
private fun BoxWithConstraintsScope.PortraitSheet(
    expansion: Animatable<Float, AnimationVector1D>,
    fraction: Float,
    state: PlaybackUiState,
    isFavorite: Boolean,
    callbacks: NowPlayingCallbacks,
    bottomReserve: androidx.compose.ui.unit.Dp,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    scope: kotlinx.coroutines.CoroutineScope,
) {
    val totalHeightDp = maxHeight
    val totalHeightPx = constraints.maxHeight.toFloat()
    val sheetHeight = lerp(Dimens.miniPlayerHeight, totalHeightDp, fraction)
    val bottomInset = bottomReserve * (1f - fraction)

    // ---- Race-free drag/settle: the release-only "locked half-open sheet" fix ----
    // Previously each drag delta did `scope.launch { expansion.snapTo() }` and the gesture end
    // did a SEPARATE `scope.launch { expansion.animateTo() }`. Those independent coroutines drain
    // on AndroidUiDispatcher in an order that isn't guaranteed, so a trailing snapTo could start
    // AFTER the settle, cancel it via the Animatable's MutatorMutex, and freeze `expansion` at a
    // partial fraction — the wedged sheet (faded mini-player + bottom nav shoved over the system
    // nav). Release timing exposed it; debug timing happened to hide it.
    //
    // Funnel every drag event through ONE long-lived consumer that is the sole writer of
    // `expansion` during a gesture, so the terminal settle can never be overtaken. The settle
    // runs as a child job so a fresh drag can still interrupt a settling animation (keeps the
    // original feel). Keyed on Unit (not totalHeightPx) so a mid-settle inset/height change can't
    // restart the consumer and cancel the settle; the live height is read via rememberUpdatedState.
    val heightPx = rememberUpdatedState(totalHeightPx)
    val dragEvents = remember { Channel<DragEvent>(Channel.UNLIMITED) }
    LaunchedEffect(Unit) {
        var settleJob: Job? = null
        for (event in dragEvents) {
            when (event) {
                is DragEvent.Delta -> {
                    settleJob?.cancel() // a fresh drag interrupts any in-flight settle
                    settleJob = null
                    val next = (expansion.value - event.delta / heightPx.value).coerceIn(0f, 1f)
                    expansion.snapTo(next)
                }
                is DragEvent.Settle -> {
                    settleJob?.cancel()
                    settleJob = launch {
                        // Velocity-aware settling: a flick up maximizes, a flick down minimizes
                        // without needing to drag the whole screen; otherwise snap to nearest.
                        val target = when {
                            event.velocity <= -FLICK_VELOCITY -> 1f
                            event.velocity >= FLICK_VELOCITY -> 0f
                            else -> if (expansion.value >= 0.5f) 1f else 0f
                        }
                        expansion.animateTo(target)
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = bottomInset)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(sheetHeight)
                .draggable(
                    orientation = Orientation.Vertical,
                    // Non-suspending hand-off to the single consumer above — no competing
                    // coroutine is launched here, so the settle can never be raced by a snap.
                    state = rememberDraggableState { delta ->
                        dragEvents.trySend(DragEvent.Delta(delta))
                    },
                    onDragStopped = { velocity ->
                        dragEvents.trySend(DragEvent.Settle(velocity))
                    },
                ),
            color = MaterialTheme.colorScheme.surface,
            // Shadow grows with expansion so the collapsed mini-player casts no dark band onto
            // the bottom nav (the previous constant 8dp left a "blackish" strip between them).
            shadowElevation = (fraction * 8f).dp,
        ) {
            Box(Modifier.fillMaxSize()) {
                // Hard hinge at 0.5 so only one of mini-player / Now Playing is ever visible —
                // avoids the overlap jank during collapse (flaw #5).
                if (fraction < 0.5f) {
                    MiniPlayer(
                        state = state,
                        onTap = { scope.launch { expansion.animateTo(1f) } },
                        onPlayPause = onPlayPause,
                        onNext = onNext,
                        modifier = Modifier.graphicsLayer { alpha = (1f - fraction * 2f).coerceIn(0f, 1f) },
                    )
                }
                if (fraction >= 0.5f) {
                    NowPlayingContent(
                        state = state,
                        isFavorite = isFavorite,
                        callbacks = callbacks,
                        modifier = Modifier
                            .statusBarsPadding()
                            .navigationBarsPadding() // keep controls above the system nav bar
                            .graphicsLayer { alpha = ((fraction - 0.5f) / 0.5f).coerceIn(0f, 1f) },
                    )
                }
            }
        }
    }
}
